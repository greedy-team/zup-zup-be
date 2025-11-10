package com.greedy.zupzup.admin.lostitem.application.dto;

import com.greedy.zupzup.category.domain.FeatureOption;
import lombok.Builder;

@Builder
public record AdminFeatureOptionDto(
        Long id,
        String optionValue,
        String quizQuestion
) {
    public static AdminFeatureOptionDto of(FeatureOption option) {
        return new AdminFeatureOptionDto(
                option.getId(),
                option.getOptionValue(),
                option.getFeature().getQuizQuestion()
        );
    }
}
