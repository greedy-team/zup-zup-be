package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.admin.lostitem.presentation.dto.UpdateLostItemCommand;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.category.exception.LostItemFeatureException;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.category.repository.FeatureOptionRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.infrastructure.S3FileCleanupService;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.application.dto.*;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class LostItemStorageService {

    private final LostItemRepository lostItemRepository;
    private final LostItemImageRepository lostItemImageRepository;
    private final LostItemFeatureRepository lostItemFeatureRepository;
    private final FeatureOptionRepository featureOptionRepository;
    private final SchoolAreaRepository schoolAreaRepository;
    private final CategoryRepository categoryRepository;
    private final S3FileCleanupService s3FileCleanupService;
    private final S3ImageFileManager s3ImageFileManager;

    /**
     * 1. 분실물 등록에 필요한 모든 데이터를 조회하고 유효성을 검사.
     */
    @Transactional(readOnly = true)
    public LostItemRegisterData getValidRegisterData(CreateLostItemCommand command) {

        Category category = getCategory(command.categoryId());
        SchoolArea foundSchoolArea = schoolAreaRepository.getAreaById(command.foundAreaId());

        if (category.isEtcCategory()) {
            return new LostItemRegisterData(category, foundSchoolArea, List.of());
        }

        List<Pair<Feature, FeatureOption>> validFeatureAndOptions = getValidFeatureAndOptions(category, command.featureOptions());
        return new LostItemRegisterData(category, foundSchoolArea, validFeatureAndOptions);
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findWithFeaturesById(categoryId)
                .orElseThrow(() -> new ApplicationException(CategoryException.CATEGORY_NOT_FOUND));
    }

    /**
     * 등록 요청된 분실물의 카테고리에 대한 특징과 옵션이 유효한지 검사하고, 유효하다면 해당 특징과 옵션의 쌍(Pair)들을 리스트로 반환합니다.
     */
    private List<Pair<Feature, FeatureOption>> getValidFeatureAndOptions(Category category,
                                                                         List<ItemFeatureOptionCommand> requestedFeatureOptions) {

        if (requestedFeatureOptions == null || requestedFeatureOptions.isEmpty() || category.getFeatures().size() != requestedFeatureOptions.size()) {
            throw new ApplicationException(LostItemException.FEATURE_REQUIRED_FOR_NON_ETC_CATEGORY);
        }

        List<FeatureOption> options = getOptions(category);
        return requestedFeatureOptions.stream()
                .map(featureOption -> {
                    Feature existsFeature = getValidFeature(category, featureOption.featureId());
                    FeatureOption existsOption = getValidFeatureOption(options, existsFeature.getId(), featureOption.optionId());
                    return Pair.of(existsFeature, existsOption);
                })
                .toList();
    }

    private List<FeatureOption> getOptions(Category category) {
        List<Long> featureIdList = category.getFeatures().stream()
                .map(Feature::getId)
                .toList();
        return featureOptionRepository.findByFeatureIds(featureIdList);
    }

    private Feature getValidFeature(Category category, Long requestedFeatureId) {
        return category.getFeatures().stream()
                .filter(feature -> feature.isValidSelection(requestedFeatureId))
                .findAny()
                .orElseThrow(() -> new ApplicationException(CategoryException.INVALID_CATEGORY_FEATURE));
    }

    private FeatureOption getValidFeatureOption(List<FeatureOption> options, Long existsFeatureId, Long requestedOptionId) {
        return options.stream()
                .filter(option -> option.isValidSelection(existsFeatureId, requestedOptionId))
                .findAny()
                .orElseThrow(() -> new ApplicationException(LostItemFeatureException.INVALID_FEATURE_OPTION));
    }


    /**
     * 2. 새로운 분실물 데이터를 등록
     */
    @Transactional
    public LostItem createNewLostItem(CreateLostItemCommand command, LostItemRegisterData validatedData, List<UploadedImageData> uploadedImages) {

        LostItem newLostItem = saveLostItem(command, validatedData.category(), validatedData.foundSchoolArea());
        saveLostItemImage(uploadedImages, newLostItem);

        if (validatedData.isNonETC()) {
            saveLostItemFeatureAndOptions(validatedData.itemFeatureAndOptions(), newLostItem);
        }

        return newLostItem;
    }

    private LostItem saveLostItem(CreateLostItemCommand command, Category category, SchoolArea foundSchoolArea) {
        LostItem newLostItem = new LostItem(command.foundAreaDetail(), command.description(), command.depositArea(), category, foundSchoolArea);
        lostItemRepository.save(newLostItem);
        return newLostItem;
    }

    private void saveLostItemImage(List<UploadedImageData> uploadedImages, LostItem newLostItem) {
        List<LostItemImage> lostItemImages = uploadedImages.stream()
                .map(image -> new LostItemImage(image.url(), image.order(), newLostItem))
                .toList();
        lostItemImageRepository.saveAll(lostItemImages);
    }

    private void saveLostItemFeatureAndOptions(List<Pair<Feature, FeatureOption>> itemFeatureAndOptions, LostItem newLostItem) {
        List<LostItemFeature> newFeatures = itemFeatureAndOptions.stream()
                .map(featureOption -> new LostItemFeature(newLostItem, featureOption.getFirst(), featureOption.getSecond()))
                .toList();
        lostItemFeatureRepository.saveAll(newFeatures);
    }

    @Transactional(readOnly = true)
    public LostItemRegisterData getValidUpdateData(UpdateLostItemCommand command) {

        Category category = getCategory(command.categoryId());
        SchoolArea foundSchoolArea = schoolAreaRepository.getAreaById(command.foundAreaId());

        if (category.isEtcCategory() || command.featureOptions() == null) {
            return new LostItemRegisterData(category, foundSchoolArea, List.of());
        }

        List<ItemFeatureOptionCommand> featureCommands =
                command.featureOptions().stream()
                        .map(opt -> new ItemFeatureOptionCommand(
                                opt.featureId(),
                                opt.optionId()
                        ))
                        .toList();

        List<Pair<Feature, FeatureOption>> validFeatureAndOptions =
                getValidFeatureAndOptions(category, featureCommands);

        return new LostItemRegisterData(category, foundSchoolArea, validFeatureAndOptions);
    }

    @Transactional
    public List<String> updateInTransaction(
            LostItem lostItem,
            UpdateLostItemCommand command,
            LostItemRegisterData validatedData,
            List<UploadedImageData> uploadedImages,
            List<LostItemImage> existingImages
    ) {
        List<String> oldKeysToDelete = existingImages.stream()
                .filter(img -> !command.keepImageIds().contains(img.getId()))
                .map(LostItemImage::getImageKey)
                .toList();

        lostItemFeatureRepository.deleteByLostItemIds(List.of(lostItem.getId()));
        if (validatedData.isNonETC()) {
            saveLostItemFeatureAndOptions(validatedData.itemFeatureAndOptions(), lostItem);
        }

        List<LostItemImage> toDeleteFromDb = existingImages.stream()
                .filter(img -> !command.keepImageIds().contains(img.getId()))
                .toList();
        lostItemImageRepository.deleteAllInBatch(toDeleteFromDb);

        saveLostItemImage(uploadedImages, lostItem);

        lostItem.updateAdmin(
                command.description(),
                command.depositArea(),
                validatedData.foundSchoolArea(),
                validatedData.category(),
                command.foundAreaDetail(),
                LostItemStatus.REGISTERED
        );

        return oldKeysToDelete;
    }

    public List<UploadedImageData> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return List.of();

        return IntStream.range(0, files.size())
                .mapToObj(i -> new UploadedImageData(s3ImageFileManager.upload(files.get(i), "lost-items"), i))
                .toList();
    }

    public void cleanupImages(List<UploadedImageData> uploadedImages) {
        uploadedImages.forEach(img -> s3ImageFileManager.delete(img.url()));
    }

    public void deleteOldImages(List<String> keys) {
        keys.forEach(s3ImageFileManager::delete);
    }
}
