package com.greedy.zupzup.admin.lostitem.application;

import com.greedy.zupzup.admin.lostitem.application.dto.AdminFeatureOptionDto;
import com.greedy.zupzup.admin.lostitem.application.dto.AdminLostItemSimpleCommand;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.repository.AdminLostItemRepository;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLostItemService {

    private final AdminLostItemRepository adminLostItemRepository;
    private final LostItemImageRepository lostItemImageRepository;
    private final S3ImageFileManager s3ImageFileManager;
    private final LostItemFeatureRepository lostItemFeatureRepository;

    @Transactional
    public ApproveLostItemsResponse approveBulk(ApproveLostItemsRequest request) {
        List<Long> lostItemIds = request.lostItemIds();

        int successCount = adminLostItemRepository.updateStatusBulkByIds(
                lostItemIds,
                LostItemStatus.REGISTERED,
                LostItemStatus.PENDING
        );

        return ApproveLostItemsResponse.of(successCount, lostItemIds.size());
    }

    @Transactional
    public RejectLostItemsResponse rejectBulk(RejectLostItemsRequest request) {
        List<Long> lostItemIds = request.lostItemIds();

        List<String> imageKeys = lostItemImageRepository.findImageKeysByLostItemIds(lostItemIds);
        imageKeys.forEach(s3ImageFileManager::delete);

        lostItemFeatureRepository.deleteByLostItemIds(lostItemIds);

        lostItemImageRepository.deleteByLostItemIds(lostItemIds);

        int deletedCount = adminLostItemRepository.deleteBulkByIds(lostItemIds);

        return RejectLostItemsResponse.of(deletedCount, lostItemIds.size());
    }

    @Transactional(readOnly = true)
    public AdminPendingLostItemListResponse getPendingLostItems(Integer page, Integer limit) {

        Pageable pageable = PageRequest.of(page - 1, limit);

        List<LostItem> items = findPendingItems(pageable);
        List<Long> ids = extractIds(items);

        Map<Long, List<String>> imageMap = loadImageMap(ids);
        Map<Long, List<AdminFeatureOptionDto>> featureMap = loadFeatureMap(ids);

        List<AdminLostItemSimpleCommand> commands = buildCommands(items, imageMap, featureMap);

        return AdminPendingLostItemListResponse.of(commands, page, limit, commands.size());
    }

    private List<LostItem> findPendingItems(Pageable pageable) {
        return adminLostItemRepository.findPendingItems(LostItemStatus.PENDING, pageable);
    }

    private List<Long> extractIds(List<LostItem> items) {
        return items.stream()
                .map(LostItem::getId)
                .toList();
    }

    private Map<Long, List<String>> loadImageMap(List<Long> ids) {
        return lostItemImageRepository.findImagesForItems(ids).stream()
                .collect(Collectors.groupingBy(
                        img -> img.getLostItem().getId(),
                        Collectors.mapping(LostItemImage::getImageKey, Collectors.toList())
                ));
    }

    private Map<Long, List<AdminFeatureOptionDto>> loadFeatureMap(List<Long> ids) {
        return lostItemFeatureRepository.findFeaturesForLostItems(ids).stream()
                .collect(Collectors.groupingBy(
                        lf -> lf.getLostItem().getId(),
                        Collectors.mapping(
                                lf -> AdminFeatureOptionDto.of(lf.getSelectedOption()),
                                Collectors.toList()
                        )
                ));
    }

    private List<AdminLostItemSimpleCommand> buildCommands(
            List<LostItem> items,
            Map<Long, List<String>> imageMap,
            Map<Long, List<AdminFeatureOptionDto>> featureMap
    ) {
        return items.stream()
                .map(item -> new AdminLostItemSimpleCommand(
                        item.getId(),
                        item.getCategory().getId(),
                        item.getCategory().getName(),
                        item.getFoundArea().getId(),
                        item.getFoundArea().getAreaName(),
                        item.getFoundAreaDetail(),
                        item.getCreatedAt().toString(),
                        item.getDescription(),
                        item.getDepositArea(),
                        imageMap.getOrDefault(item.getId(), Collections.emptyList()),
                        featureMap.getOrDefault(item.getId(), Collections.emptyList())
                ))
                .toList();
    }
}
