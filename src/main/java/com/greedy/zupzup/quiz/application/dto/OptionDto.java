package com.greedy.zupzup.quiz.application.dto;

import com.greedy.zupzup.category.domain.FeatureOption;

public record OptionDto(
        Long id,
        String text
) {

    public static OptionDto from(FeatureOption featureOption) {
        return new OptionDto(featureOption.getId(), featureOption.getOptionValue());
    }
}
