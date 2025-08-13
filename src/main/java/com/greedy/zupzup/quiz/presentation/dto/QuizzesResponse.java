package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.QuizDto;
import java.util.List;
import java.util.stream.Collectors;

public record QuizzesResponse (
    List<QuizResponse> quizzes
){

    public static QuizzesResponse from(List<QuizDto> quizDtos) {
        List<QuizResponse> quizzes = quizDtos.stream()
                .map(QuizResponse::from)
                .collect(Collectors.toList());
        return new QuizzesResponse(quizzes);
    }
}
