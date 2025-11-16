package com.greedy.zupzup.quiz.application.dto;

import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import java.util.List;

public record QuizData(
        Long featureId,
        String question,
        List<OptionData> options
) {

    public static QuizData of(LostItemFeature lostItemFeature, List<OptionData> options) {
        return new QuizData(
                lostItemFeature.getFeature().getId(),
                lostItemFeature.getFeature().getQuizQuestion(),
                options
        );
    }
}
