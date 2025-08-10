package com.greedy.zupzup.category.application.dto;

import com.greedy.zupzup.category.domain.Feature;

import java.util.List;

public record FeatureWithOptionsDto(
        Long id,
        String name,
        String quizQuestion,
        List<FeatureOptionDto> options
) {
    public static FeatureWithOptionsDto of(Feature entity) {
        return new FeatureWithOptionsDto(
                entity.getId(),
                entity.getName(),
                entity.getQuizQuestion(),
                entity.getOptions().stream().map(FeatureOptionDto::of).toList()
        );
    }
}
