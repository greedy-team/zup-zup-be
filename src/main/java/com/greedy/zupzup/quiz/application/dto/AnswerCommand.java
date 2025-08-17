package com.greedy.zupzup.quiz.application.dto;

import com.greedy.zupzup.quiz.presentation.dto.AnswerRequest;

public record AnswerCommand(
        Long featureId,
        Long selectedOptionId
) {

    public static AnswerCommand from(AnswerRequest request) {
        return new AnswerCommand(request.featureId(), request.selectedOptionId());
    }
}
