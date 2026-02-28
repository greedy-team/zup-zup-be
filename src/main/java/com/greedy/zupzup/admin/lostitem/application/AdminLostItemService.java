package com.greedy.zupzup.admin.lostitem.application;

import com.greedy.zupzup.admin.lostitem.presentation.dto.LostItemFeatureOptionDto;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminLostItemDto;
import com.greedy.zupzup.admin.lostitem.application.dto.ItemImageBulkDeletedEvent;
import com.greedy.zupzup.admin.lostitem.application.dto.UpdateLostItemResult;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.application.dto.UpdateLostItemCommand;
import com.greedy.zupzup.admin.lostitem.repository.AdminLostItemRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.LostItemStorageService;
import com.greedy.zupzup.lostitem.application.dto.LostItemRegisterData;
import com.greedy.zupzup.lostitem.application.dto.UploadedImageData;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLostItemService {

    private final AdminLostItemRepository adminLostItemRepository;
    private final LostItemImageRepository lostItemImageRepository;
    private final LostItemFeatureRepository lostItemFeatureRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LostItemStorageService lostItemStorageService;

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

        List<String> imageUrls = lostItemImageRepository.findImageKeysByLostItemIds(lostItemIds);

        lostItemFeatureRepository.deleteByLostItemIds(lostItemIds);
        lostItemImageRepository.deleteByLostItemIds(lostItemIds);
        int deletedCount = adminLostItemRepository.deleteBulkByIds(lostItemIds);

        eventPublisher.publishEvent(ItemImageBulkDeletedEvent.from(imageUrls));
        return RejectLostItemsResponse.of(deletedCount, lostItemIds.size());
    }

    @Transactional(readOnly = true)
    public AdminPendingLostItemListResponse getPendingLostItems(Integer page, Integer limit) {

        Pageable pageable = PageRequest.of(page - 1, limit);

        List<LostItem> items = findPendingItems(pageable);
        List<Long> ids = extractIds(items);

        Map<Long, List<String>> imageMap = loadImageMap(ids);
        Map<Long, List<LostItemFeatureOptionDto>> featureMap = loadFeatureMap(ids);

        List<AdminLostItemDto> results =
                AdminLostItemDto.fromList(items, imageMap, featureMap);
        return AdminPendingLostItemListResponse.of(results, page, limit, results.size());
    }

    public UpdateLostItemResult updateLostItem(UpdateLostItemCommand command) {

        LostItem lostItem = findPendingLostItem(command.lostItemId());
        List<LostItemImage> existingImages = lostItemImageRepository.findByLostItemId(command.lostItemId());
        validateImageOwnership(existingImages, command.keepImageIds());

        LostItemRegisterData validatedData = lostItemStorageService.getValidUpdateData(command);

        List<UploadedImageData> uploadedImages = lostItemStorageService.uploadImages(command.newImages());

        try {
            List<String> oldKeysToDelete = lostItemStorageService.updateInTransaction(
                    lostItem,
                    command,
                    validatedData,
                    uploadedImages,
                    existingImages
            );

            lostItemStorageService.deleteOldImages(oldKeysToDelete);

            return UpdateLostItemResult.from(command.lostItemId());

        } catch (Exception e) {
            lostItemStorageService.cleanupImages(uploadedImages);
            throw e;
        }
    }

    private void validateImageOwnership(List<LostItemImage> existingImages, List<Long> keepImageIds) {
        if (keepImageIds == null || keepImageIds.isEmpty()) return;
        Set<Long> existingIds = existingImages.stream()
                .map(LostItemImage::getId)
                .collect(Collectors.toSet());
        if (!existingIds.containsAll(keepImageIds)) {
            throw new ApplicationException(LostItemException.INVALID_IMAGE_ACCESS);
        }
    }

    private LostItem findPendingLostItem(Long lostItemId) {
        LostItem lostItem = adminLostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));
        if (lostItem.getStatus() != LostItemStatus.PENDING) {
            throw new ApplicationException(LostItemException.ACCESS_FORBIDDEN);
        }
        return lostItem;
    }

    private List<LostItem> findPendingItems(Pageable pageable) {
        return adminLostItemRepository.findPendingItems(LostItemStatus.PENDING, pageable);
    }

    private List<Long> extractIds(List<LostItem> items) {
        return items.stream().map(LostItem::getId).toList();
    }

    private Map<Long, List<String>> loadImageMap(List<Long> ids) {
        return lostItemImageRepository.findImagesForItems(ids).stream()
                .collect(Collectors.groupingBy(
                        img -> img.getLostItem().getId(),
                        Collectors.mapping(LostItemImage::getImageKey, Collectors.toList())
                ));
    }

    private Map<Long, List<LostItemFeatureOptionDto>> loadFeatureMap(List<Long> ids) {
        return lostItemFeatureRepository.findFeaturesForLostItems(ids).stream()
                .collect(Collectors.groupingBy(
                        lf -> lf.getLostItem().getId(),
                        Collectors.mapping(lf -> LostItemFeatureOptionDto.of(lf.getSelectedOption()), Collectors.toList())
                ));
    }
}
