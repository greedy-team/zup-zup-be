package com.greedy.zupzup.admin.lostitem.application;

import com.greedy.zupzup.admin.lostitem.application.dto.AdminLostItemSimpleCommand;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.repository.AdminLostItemRepository;
import com.greedy.zupzup.category.application.dto.FeatureOptionDto;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
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

        List<LostItem> items = adminLostItemRepository.findPendingItems(LostItemStatus.PENDING, pageable);

        List<Long> ids = items.stream().map(LostItem::getId).toList();

        List<LostItemImage> images = lostItemImageRepository.findImagesForItems(ids);
        Map<Long, List<String>> imgMap = images.stream()
                .collect(Collectors.groupingBy(
                        img -> img.getLostItem().getId(),
                        Collectors.mapping(LostItemImage::getImageKey, Collectors.toList())
                ));

        List<LostItemFeature> features = lostItemFeatureRepository.findFeaturesForLostItems(ids);
        Map<Long, List<FeatureOptionDto>> featuresMap = features.stream()
                .collect(Collectors.groupingBy(
                        lf -> lf.getLostItem().getId(),
                        Collectors.mapping(
                                lf -> FeatureOptionDto.of(lf.getSelectedOption()),
                                Collectors.toList()
                        )
                ));

        List<AdminLostItemSimpleCommand> commands = items.stream()
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
                        imgMap.getOrDefault(item.getId(), Collections.emptyList()),
                        featuresMap.getOrDefault(item.getId(), Collections.emptyList())
                ))
                .toList();

        return AdminPendingLostItemListResponse.of(
                commands,
                page,
                limit,
                commands.size()
        );
    }

}
