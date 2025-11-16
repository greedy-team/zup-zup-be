package com.greedy.zupzup.quiz.application.dto;

public record QuizResultDto(
        boolean correct
) {
    public static QuizResultDto ofCorrect() {
        return new QuizResultDto(true);
    }

    public static QuizResultDto ofIncorrect() {
        return new QuizResultDto(false);
    }
}
