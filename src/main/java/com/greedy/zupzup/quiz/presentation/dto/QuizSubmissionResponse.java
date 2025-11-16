package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.QuizResultDto;

public record QuizSubmissionResponse(
        boolean correct
) {

    public static QuizSubmissionResponse from(QuizResultDto resultDto) {
        return new QuizSubmissionResponse(resultDto.correct());
    }
}
