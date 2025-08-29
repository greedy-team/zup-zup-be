package com.greedy.zupzup.quiz.repository;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
import com.greedy.zupzup.quiz.exception.QuizException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    boolean existsByLostItemIdAndMemberId(Long lostItemId, Long memberId);

    boolean existsByLostItem_IdAndMember_IdAndIsCorrectTrue(Long lostItemId, Long memberId);

    Optional<QuizAttempt> findByLostItemIdAndMemberId(Long lostItemId, Long memberId);

    default QuizAttempt getByLostItemIdAndMemberId(Long lostItemId, Long memberId) {
        return findByLostItemIdAndMemberId(lostItemId, memberId)
                .orElseThrow(() -> new ApplicationException(QuizException.QUIZ_NOT_PASSED));
    }
}
