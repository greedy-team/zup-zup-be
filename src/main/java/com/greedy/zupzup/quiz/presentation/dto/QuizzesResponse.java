package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.QuizData;
import java.util.List;
import java.util.stream.Collectors;

public record QuizzesResponse (
    List<QuizResponse> quizzes
){

    public static QuizzesResponse from(List<QuizData> quizData) {
        List<QuizResponse> quizzes = quizData.stream()
                .map(QuizResponse::from)
                .collect(Collectors.toList());
        return new QuizzesResponse(quizzes);
    }
}
