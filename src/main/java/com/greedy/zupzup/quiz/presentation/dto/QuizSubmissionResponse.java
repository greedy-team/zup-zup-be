package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.QuizResultDto;

public record QuizSubmissionResponse(
        boolean correct,
        LostItemDetailResponse detail
) {

    public static QuizSubmissionResponse from(QuizResultDto resultDto) {
        LostItemDetailResponse detailResponse = resultDto.detail() != null
                ? LostItemDetailResponse.from(resultDto.detail())
                : null;
        return new QuizSubmissionResponse(resultDto.correct(), detailResponse);
    }
}
