package com.greedy.zupzup.category.application.dto;

import com.greedy.zupzup.category.domain.FeatureOption;

public record FeatureOptionDto(Long id, String optionValue) {
    public static FeatureOptionDto of(FeatureOption entity) {
        return new FeatureOptionDto(entity.getId(), entity.getOptionValue());
    }
}
