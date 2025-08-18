package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.lostitem.repository.RepresentativeImageProjection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LostItemListService {

    private final LostItemRepository lostItemRepository;
    private final LostItemImageRepository lostItemImageRepository;

    @Transactional(readOnly = true)
    public Page<LostItemListCommand> getLostItems(Long categoryId, Long schoolAreaId, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return lostItemRepository
                .findList(categoryId, schoolAreaId, LostItemStatus.REGISTERED, pageable)
                .map(LostItemListCommand::from);
    }

    @Transactional(readOnly = true)
    public Map<Long, String> getRepresentativeImageMapByItemIds(List<Long> lostItemIds) {
        return lostItemImageRepository.findRepresentativeImages(lostItemIds).stream()
                .collect(Collectors.toMap(
                        RepresentativeImageProjection::getLostItemId,
                        RepresentativeImageProjection::getImageUrl
                ));
    }
}
