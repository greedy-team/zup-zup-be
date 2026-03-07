package com.greedy.zupzup.quiz.repository;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
import com.greedy.zupzup.quiz.exception.QuizException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    boolean existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(Long lostItemId, Long memberId);

    @Query("""
            select qa.lostItem.id
            from QuizAttempt qa
            where qa.member.id = :memberId
              and qa.isCorrect = false
            """)
    List<Long> findFailedLostItemIdsByMemberId(@Param("memberId") Long memberId);

    boolean existsByLostItem_IdAndMember_IdAndIsCorrectTrue(Long lostItemId, Long memberId);

    Optional<QuizAttempt> findByLostItem_IdAndMember_Id(Long lostItemId, Long memberId);

    default QuizAttempt getByLostItemIdAndMemberId(Long lostItemId, Long memberId) {
        return findByLostItem_IdAndMember_Id(lostItemId, memberId)
                .orElseThrow(() -> new ApplicationException(QuizException.QUIZ_NOT_PASSED));
    }
}
