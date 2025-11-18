package com.greedy.zupzup.quiz.application.dto;

public record QuizResult(
        boolean correct
) {
    public static QuizResult ofCorrect() {
        return new QuizResult(true);
    }

    public static QuizResult ofIncorrect() {
        return new QuizResult(false);
    }
}
