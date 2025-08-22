package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.quiz.domain.QuizAttempt;

public class QuizAttemptFixture {

    public static QuizAttempt INCORRECT_QUIZ_ATTEMPT(Member member, LostItem lostItem) {
        return QuizAttempt.builder()
                .member(member)
                .lostItem(lostItem)
                .isCorrect(false)
                .build();
    }

    public static QuizAttempt CORRECT_QUIZ_ATTEMPT(Member member, LostItem lostItem) {
        return QuizAttempt.builder()
                .member(member)
                .lostItem(lostItem)
                .isCorrect(true)
                .build();
    }
}

