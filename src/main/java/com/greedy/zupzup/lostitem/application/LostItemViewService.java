package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.LostItemSimpleViewCommand;
import com.greedy.zupzup.lostitem.application.dto.MyPledgedLostItemCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.MyPledgedLostItemProjection;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.lostitem.repository.RepresentativeImageProjection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LostItemViewService {

    private final LostItemRepository lostItemRepository;
    private final LostItemImageRepository lostItemImageRepository;

    /**
     * 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<LostItemListCommand> getLostItems(Long categoryId, Long schoolAreaId, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return lostItemRepository
                .findList(categoryId, schoolAreaId, LostItemStatus.REGISTERED, pageable)
                .map(LostItemListCommand::from);
    }

    /**
     * 단건 조회
     */
    @Transactional(readOnly = true)
    public LostItemSimpleViewCommand getSimpleView(Long lostItemId) {
        LostItem item = lostItemRepository.getWithCategoryById(lostItemId);

        statusGuardForSimpleView(item);

        boolean isEtc = item.isEtcCategory();

        if (!isEtc) {
            return LostItemSimpleViewCommand.of(item, Objects.requireNonNull(item.getCategory()).getIconUrl());
        }

        String rep = getRepresentativeImageMapByItemIds(List.of(lostItemId))
                .getOrDefault(lostItemId, "");
        return LostItemSimpleViewCommand.of(item, rep);
    }

    /**
     * 대표 이미지 맵 조회
     */
    public Map<Long, String> getRepresentativeImageMapByItemIds(List<Long> lostItemIds) {
        return lostItemImageRepository.findRepresentativeImages(lostItemIds).stream()
                .collect(Collectors.toMap(
                        RepresentativeImageProjection::getLostItemId,
                        RepresentativeImageProjection::getImageUrl
                ));
    }

    private void statusGuardForSimpleView(LostItem item) {
        LostItemStatus status = item.getStatus();
        if (status == LostItemStatus.REGISTERED) {
            return;
        }

        LostItemException code = switch (status) {
            case PLEDGED -> LostItemException.ACCESS_FORBIDDEN_PLEDGED;
            case FOUND -> LostItemException.ACCESS_FORBIDDEN_FOUND;
            default -> LostItemException.ACCESS_FORBIDDEN;
        };

        throw new ApplicationException(code);
    }

}
