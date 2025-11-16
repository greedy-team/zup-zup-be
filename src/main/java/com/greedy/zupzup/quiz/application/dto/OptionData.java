package com.greedy.zupzup.quiz.application.dto;

import com.greedy.zupzup.category.domain.FeatureOption;

public record OptionData(
        Long id,
        String text
) {

    public static OptionData from(FeatureOption featureOption) {
        return new OptionData(featureOption.getId(), featureOption.getOptionValue());
    }
}
