package com.greedy.zupzup.admin.lostitem.application;

import com.greedy.zupzup.admin.lostitem.application.dto.AdminFeatureOptionDto;
import com.greedy.zupzup.admin.lostitem.application.dto.AdminLostItemResult;
import com.greedy.zupzup.admin.lostitem.application.dto.ItemImageBulkDeletedEvent;
import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.UpdateLostItemRequest;
import com.greedy.zupzup.admin.lostitem.repository.AdminLostItemRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.infrastructure.S3FileCleanupService;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.application.LostItemStorageService;
import com.greedy.zupzup.lostitem.application.dto.CreateLostItemCommand;
import com.greedy.zupzup.lostitem.application.dto.LostItemRegisterData;
import com.greedy.zupzup.lostitem.application.dto.UploadedImageData;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.exception.LostItemImageException;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminLostItemService {

    private final AdminLostItemRepository adminLostItemRepository;
    private final LostItemImageRepository lostItemImageRepository;
    private final LostItemFeatureRepository lostItemFeatureRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LostItemStorageService lostItemStorageService;
    private final S3ImageFileManager s3ImageFileManager;
    private final S3FileCleanupService s3FileCleanupService;

    private static final String IMAGE_DIRECTORY = "lost-item-images";

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
        Map<Long, List<AdminFeatureOptionDto>> featureMap = loadFeatureMap(ids);

        List<AdminLostItemResult> commands = buildCommands(items, imageMap, featureMap);

        return AdminPendingLostItemListResponse.of(commands, page, limit, commands.size());
    }

    @Transactional
    public void updateLostItem(Long lostItemId,
                               UpdateLostItemRequest request,
                               List<Long> keepImageIds,
                               List<MultipartFile> newImages) {

        LostItem lostItem = findPendingLostItem(lostItemId);

        validateImageCount(keepImageIds, newImages);

        LostItemRegisterData validData = getValidatedRegisterData(request);

        List<UploadedImageData> uploadedNewImages = syncImages(lostItem, keepImageIds, newImages);

        try {
            updateDomainInfo(lostItem, request, validData);
            updateFeatures(lostItem, validData);

            lostItem.approve();

        } catch (Exception e) {
            cleanupNewlyUploadedImages(uploadedNewImages);
            throw e;
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

    private void validateImageCount(List<Long> keepImageIds, List<MultipartFile> newImages) {
        int keepCount = (keepImageIds == null) ? 0 : keepImageIds.size();
        int newCount = (newImages == null) ? 0 : newImages.size();
        if (keepCount + newCount < 1) {
            throw new ApplicationException(LostItemImageException.INVALID_IMAGE_COUNT);
        }
    }

    private LostItemRegisterData getValidatedRegisterData(UpdateLostItemRequest request) {
        CreateLostItemCommand dummyCommand = new CreateLostItemCommand(
                request.description(),
                request.depositArea(),
                request.foundAreaId(),
                request.foundAreaDetail(),
                request.categoryId(),
                CreateLostItemCommand.toItemFeatureOptionList(request.featureOptions()),
                List.of()
        );
        return lostItemStorageService.getValidRegisterData(dummyCommand);
    }

    private List<UploadedImageData> syncImages(LostItem lostItem, List<Long> keepImageIds, List<MultipartFile> newImages) {
        List<LostItemImage> existingImages = lostItemImageRepository.findByLostItemId(lostItem.getId());
        List<Long> safeKeepIds = (keepImageIds == null) ? List.of() : keepImageIds;

        validateImageOwnership(lostItem.getId(), safeKeepIds);

        deleteExcludedImages(existingImages, safeKeepIds);

        return uploadAndSaveNewImages(lostItem, newImages);
    }

    private void validateImageOwnership(Long lostItemId, List<Long> safeKeepIds) {
        if (!safeKeepIds.isEmpty()) {
            long count = lostItemImageRepository.countByIdInAndLostItemId(safeKeepIds, lostItemId);
            if (count != safeKeepIds.size()) {
                throw new ApplicationException(LostItemException.INVALID_IMAGE_ACCESS);
            }
        }
    }

    private void deleteExcludedImages(List<LostItemImage> existingImages, List<Long> safeKeepIds) {
        List<LostItemImage> toDelete = existingImages.stream()
                .filter(img -> !safeKeepIds.contains(img.getId()))
                .toList();

        if (!toDelete.isEmpty()) {
            lostItemImageRepository.deleteAll(toDelete);
            s3FileCleanupService.cleanupOrphanFiles(
                    toDelete.stream().map(LostItemImage::getImageKey).toList()
            );
        }
    }

    private List<UploadedImageData> uploadAndSaveNewImages(LostItem lostItem, List<MultipartFile> newImages) {
        if (newImages == null || newImages.isEmpty()) {
            return List.of();
        }

        List<UploadedImageData> uploaded = uploadNewImages(newImages);
        lostItemImageRepository.saveAll(
                uploaded.stream()
                        .map(data -> LostItemImage.of(lostItem, data.url(), data.order()))
                        .toList()
        );
        return uploaded;
    }

    private void updateDomainInfo(LostItem lostItem, UpdateLostItemRequest request, LostItemRegisterData validData) {
        lostItem.updateInfo(
                request.description(),
                request.depositArea(),
                validData.foundSchoolArea(),
                request.foundAreaDetail(),
                validData.category()
        );
    }

    private void cleanupNewlyUploadedImages(List<UploadedImageData> uploadedNewImages) {
        if (!uploadedNewImages.isEmpty()) {
            s3FileCleanupService.cleanupOrphanFiles(
                    uploadedNewImages.stream().map(UploadedImageData::url).toList()
            );
        }
    }

    private List<UploadedImageData> uploadNewImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return IntStream.range(0, images.size())
                .mapToObj(i -> {
                    String url = s3ImageFileManager.upload(images.get(i), IMAGE_DIRECTORY);
                    return new UploadedImageData(url, i);
                }).toList();
    }

    private void updateFeatures(LostItem lostItem, LostItemRegisterData validData) {
        lostItemFeatureRepository.deleteByLostItemIds(List.of(lostItem.getId()));
        if (validData.isNonETC()) {
            List<LostItemFeature> newFeatures = validData.itemFeatureAndOptions().stream()
                    .map(pair -> LostItemFeature.of(lostItem, pair.getFirst(), pair.getSecond()))
                    .toList();
            lostItemFeatureRepository.saveAll(newFeatures);
        }
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

    private List<AdminLostItemResult> buildCommands(
            List<LostItem> items,
            Map<Long, List<String>> imageMap,
            Map<Long, List<AdminFeatureOptionDto>> featureMap
    ) {
        return items.stream()
                .map(item -> new AdminLostItemResult(
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
