package com.greedy.zupzup.quiz.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull(message = "퀴즈의 특징을 선택해 주세요.")
        Long featureId,

        @NotNull(message = "퀴즈의 답변을 선택해 주세요.")
        Long selectedOptionId
) {

}
