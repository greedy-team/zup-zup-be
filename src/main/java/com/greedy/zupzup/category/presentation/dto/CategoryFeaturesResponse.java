package com.greedy.zupzup.category.presentation.dto;

import com.greedy.zupzup.category.application.dto.FeatureWithOptionsDto;
import com.greedy.zupzup.category.domain.Category;

import java.util.List;

public record CategoryFeaturesResponse(
        Long categoryId,
        String categoryName,
        List<FeatureWithOptionsDto> features
) {
    public static CategoryFeaturesResponse of(Category category) {
        List<FeatureWithOptionsDto> features = category.getFeatures()
                .stream()
                .map(FeatureWithOptionsDto::of)
                .toList();

        return new CategoryFeaturesResponse(category.getId(), category.getName(), features);
    }
}
