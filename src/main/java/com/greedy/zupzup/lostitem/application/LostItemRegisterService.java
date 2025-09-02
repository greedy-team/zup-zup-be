package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.category.exception.LostItemFeatureException;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.category.repository.FeatureOptionRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.application.dto.CreateImageCommand;
import com.greedy.zupzup.lostitem.application.dto.CreateLostItemCommand;
import com.greedy.zupzup.lostitem.application.dto.ItemFeatureOptionCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemRegisterService {

    private static final String IMAGE_DIRECTORY = "lost-item-images";

    private final LostItemRepository lostItemRepository;
    private final LostItemImageRepository lostItemImageRepository;
    private final LostItemFeatureRepository lostItemFeatureRepository;
    private final FeatureOptionRepository featureOptionRepository;
    private final SchoolAreaRepository schoolAreaRepository;
    private final CategoryRepository categoryRepository;
    private final S3ImageFileManager s3ImageFileManager;

    @Transactional
    public LostItem registLostItem(CreateLostItemCommand command) {

        Category category = getCategory(command.categoryId());

        if (category.isNotQuizCategory()){
            return registETCLostItem(command, category);
        }

        return registNonETCLostItem(command, category);
    }

    private LostItem registETCLostItem(CreateLostItemCommand command, Category category) {
        SchoolArea foundSchoolArea = schoolAreaRepository.getAreaById(command.foundAreaId());
        LostItem newLostItem = saveLostItem(command, category, foundSchoolArea);
        saveLostItemImage(command.images(), newLostItem);
        return newLostItem;
    }

    private LostItem registNonETCLostItem(CreateLostItemCommand command, Category category) {
        if (command.featureOptions() == null || command.featureOptions().isEmpty()) {
            throw new ApplicationException(LostItemException.FEATURE_REQUIRED_FOR_NON_ETC_CATEGORY);
        }

        List<Pair<Feature, FeatureOption>> itemFeatureAndOptions = getValidFeatureAndOptions(category, command.featureOptions());
        SchoolArea foundSchoolArea = schoolAreaRepository.getAreaById(command.foundAreaId());
        LostItem newLostItem = saveLostItem(command, category, foundSchoolArea);
        saveLostItemImage(command.images(), newLostItem);
        saveLostItemFeatureAndOptions(itemFeatureAndOptions, newLostItem);
        return newLostItem;
    }

    private LostItem saveLostItem(CreateLostItemCommand command, Category category, SchoolArea foundSchoolArea) {
        LostItem newLostItem = new LostItem(command.foundAreaDetail(), command.description(), command.depositArea(), category, foundSchoolArea);
        lostItemRepository.save(newLostItem);
        return newLostItem;
    }

    private void saveLostItemImage(List<CreateImageCommand> images, LostItem newLostItem) {
        List<LostItemImage> newLostItemImages = images.stream()
                .map(image -> {
                    String imageURL = s3ImageFileManager.upload(image.imageFile(), IMAGE_DIRECTORY);
                    return new LostItemImage(imageURL, image.order(), newLostItem);
                }).toList();
        lostItemImageRepository.saveAll(newLostItemImages);
    }

    private void saveLostItemFeatureAndOptions(List<Pair<Feature, FeatureOption>> itemFeatureAndOptions, LostItem newLostItem) {
        List<LostItemFeature> newFeatures = itemFeatureAndOptions.stream()
                .map(featureOption -> new LostItemFeature(newLostItem, featureOption.getFirst(), featureOption.getSecond()))
                .toList();
        lostItemFeatureRepository.saveAll(newFeatures);
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

    private FeatureOption getValidFeatureOption(List<FeatureOption> options, Long existsFeatureId , Long requestedOptionId) {
        return options.stream()
                .filter(option -> option.isValidSelection(existsFeatureId, requestedOptionId))
                .findAny()
                .orElseThrow(() -> new ApplicationException(LostItemFeatureException.INVALID_FEATURE_OPTION));
    }

}
