package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.FoundItemListResult;
import com.greedy.zupzup.lostitem.application.dto.GetItemListCommand;
import com.greedy.zupzup.lostitem.application.dto.LostItemSimpleViewResult;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.application.dto.LostItemListResult;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public Page<LostItemListResult> getLostItems(GetItemListCommand command) {
        return lostItemRepository
                .findList(command.categoryId(), command.schoolAreaId(), LostItemStatus.REGISTERED, command.pageable())
                .map(LostItemListResult::from);
    }

    /**
     * 단건 조회
     */
    @Transactional(readOnly = true)
    public LostItemSimpleViewResult getSimpleView(Long lostItemId) {
        LostItem item = lostItemRepository.getWithCategoryById(lostItemId);

        statusGuardForSimpleView(item);

        boolean isEtc = item.isEtcCategory();

        if (!isEtc) {
            return LostItemSimpleViewResult.of(item, Objects.requireNonNull(item.getCategory()).getIconUrl());
        }

        String rep = getRepresentativeImageMapByItemIds(List.of(lostItemId))
                .getOrDefault(lostItemId, "");
        return LostItemSimpleViewResult.of(item, rep);
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


    /**
     * 찾아진 분실물 조회
     */
    @Transactional(readOnly = true)
    public Page<FoundItemListResult> getFoundItems(GetItemListCommand command) {
        return lostItemRepository.findFoundItems(command.categoryId(), command.schoolAreaId(), command.pageable())
                .map(FoundItemListResult::from);
    }

}
