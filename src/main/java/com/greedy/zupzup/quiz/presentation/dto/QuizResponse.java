package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.QuizDto;
import java.util.List;
import java.util.stream.Collectors;

public record QuizResponse(
        Long featureId,
        String question,
        List<QuizOption> options
) {

    public static QuizResponse from(QuizDto quizDto) {
        List<QuizOption> quizOptions = quizDto.options().stream()
                .map(optionInfo -> new QuizOption(optionInfo.id(), optionInfo.text()))
                .collect(Collectors.toList());
        return new QuizResponse(quizDto.featureId(), quizDto.question(), quizOptions);
    }
}
