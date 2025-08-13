package com.greedy.zupzup.quiz.repository;

import com.greedy.zupzup.quiz.domain.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    boolean existsByLostItemIdAndMemberId(Long lostItemId, Long memberId);
}
