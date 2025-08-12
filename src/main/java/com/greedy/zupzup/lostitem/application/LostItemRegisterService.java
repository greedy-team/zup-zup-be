package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.category.exception.LostItemFeatureException;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.CreateLostItemCommand;
import com.greedy.zupzup.lostitem.application.dto.ItemFeatureOptionCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LostItemRegisterService {

    private final LostItemRepository lostItemRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public LostItem registLostItem(CreateLostItemCommand command) {

        Category category = getCategory(command);

        validateFeatureAndOptionExists(category, command.featureOptions());

    }

    private Category getCategory(CreateLostItemCommand command) {
        return categoryRepository.findWithFeatureAndOptionById(command.categoryId())
                .orElseThrow(() -> new ApplicationException(CategoryException.CATEGORY_NOT_FOUND));
    }

    private void validateFeatureAndOptionExists(Category category, List<ItemFeatureOptionCommand> requestedFeatureOptions) {
        requestedFeatureOptions.forEach(featureOption -> {
            Feature existsFeature = getexistsFeature(category, featureOption.featureId());
            validateOptionExists(existsFeature, featureOption.optionId());
        });
    }

    private Feature getexistsFeature(Category category, Long requestedFeatureId) {
        return category.getFeatures().stream()
                .filter(feature -> feature.getId().equals(requestedFeatureId))
                .findAny()
                .orElseThrow(() -> new ApplicationException(CategoryException.INVALID_CATEGORY_FEATURE));
    }

    private void validateOptionExists(Feature correctFeature, Long requestedOptionId) {
        correctFeature.getOptions().stream()
                .filter(option -> option.getId().equals(requestedOptionId))
                .findAny()
                .orElseThrow(() -> new ApplicationException(LostItemFeatureException.INVALID_FEATURE_OPTION));
    }



}
