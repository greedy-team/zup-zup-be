package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.category.exception.LostItemFeatureException;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.CreateLostItemCommand;
import com.greedy.zupzup.lostitem.application.dto.ItemFeatureOptionCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemRegisterService {

    private final LostItemRepository lostItemRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public LostItem registLostItem(CreateLostItemCommand command) {

        Category category = getCategory(command);

        List<Pair<Feature, FeatureOption>> itemFeatureAndOptions = getValidFeatureAndOptions(category, command.featureOptions());


    }

    private Category getCategory(CreateLostItemCommand command) {
        return categoryRepository.findWithFeatureAndOptionById(command.categoryId())
                .orElseThrow(() -> new ApplicationException(CategoryException.CATEGORY_NOT_FOUND));
    }


    /**
     * 등록 요청된 분실물의 테마에 대한 특징과 옵션이 유효한지 검사하고, 유효하다면 해당 특징과 옵션의 쌍(Pair)들을 리스트로 반환합니다.
     */
    private List<Pair<Feature, FeatureOption>> getValidFeatureAndOptions(Category category,
                                                                         List<ItemFeatureOptionCommand> requestedFeatureOptions) {
        return requestedFeatureOptions.stream()
                .map(featureOption -> {
                    Feature existsFeature = getValidFeature(category, featureOption.featureId());
                    FeatureOption existsOption = getValidFeatureOption(existsFeature, featureOption.optionId());
                    return Pair.of(existsFeature, existsOption);
                })
                .toList();
    }

    private Feature getValidFeature(Category category, Long requestedFeatureId) {
        return category.getFeatures().stream()
                .filter(feature -> feature.getId().equals(requestedFeatureId))
                .findAny()
                .orElseThrow(() -> new ApplicationException(CategoryException.INVALID_CATEGORY_FEATURE));
    }

    private FeatureOption getValidFeatureOption(Feature feature, Long requestedOptionId) {
        return feature.getOptions().stream()
                .filter(option -> option.getId().equals(requestedOptionId))
                .findAny()
                .orElseThrow(() -> new ApplicationException(LostItemFeatureException.INVALID_FEATURE_OPTION));
    }



}
