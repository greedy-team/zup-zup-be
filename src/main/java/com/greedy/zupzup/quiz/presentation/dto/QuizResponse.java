package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.QuizData;
import java.util.List;
import java.util.stream.Collectors;

public record QuizResponse(
        Long featureId,
        String question,
        List<OptionData> options
) {

    public static QuizResponse from(QuizData quizData) {
        List<OptionData> optionData = quizData.options().stream()
                .map(optionInfo -> new OptionData(optionInfo.id(), optionInfo.text()))
                .collect(Collectors.toList());
        return new QuizResponse(quizData.featureId(), quizData.question(), optionData);
    }
}
