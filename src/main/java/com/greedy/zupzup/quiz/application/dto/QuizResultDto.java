package com.greedy.zupzup.quiz.application.dto;

import com.greedy.zupzup.lostitem.domain.LostItem;

public record QuizResultDto(
        boolean correct,
        LostItemDetailDto detail
) {
    public static QuizResultDto correct(LostItem lostItem) {
        return new QuizResultDto(true, LostItemDetailDto.from(lostItem));
    }

    public static QuizResultDto incorrect() {
        return new QuizResultDto(false, null);
    }
}
