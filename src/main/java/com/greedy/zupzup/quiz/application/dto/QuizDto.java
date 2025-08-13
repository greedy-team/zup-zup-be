package com.greedy.zupzup.quiz.application.dto;

import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import java.util.List;

public record QuizDto(
        Long featureId,
        String question,
        List<OptionDto> options
) {

    public static QuizDto of(LostItemFeature lostItemFeature, List<OptionDto> options) {
        return new QuizDto(
                lostItemFeature.getFeature().getId(),
                lostItemFeature.getFeature().getQuizQuestion(),
                options
        );
    }
}
