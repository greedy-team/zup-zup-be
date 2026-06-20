package com.greedy.zupzup.admin.lostitem.presentation.dto;

import com.greedy.zupzup.category.domain.FeatureOption;
import lombok.Builder;

@Builder
public record LostItemFeatureOptionDto(
        Long id,
        String optionValue,
        String quizQuestion
) {
    public static LostItemFeatureOptionDto of(FeatureOption option) {
        return new LostItemFeatureOptionDto(
                option.getId(),
                option.getOptionValue(),
                option.getFeature().getQuizQuestion()
        );
    }
}
