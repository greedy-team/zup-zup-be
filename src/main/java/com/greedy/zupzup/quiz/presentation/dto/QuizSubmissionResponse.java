package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.QuizResult;

public record QuizSubmissionResponse(
        boolean correct
) {

    public static QuizSubmissionResponse from(QuizResult resultDto) {
        return new QuizSubmissionResponse(resultDto.correct());
    }
}
