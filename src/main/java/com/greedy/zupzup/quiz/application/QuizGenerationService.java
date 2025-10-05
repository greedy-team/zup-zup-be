package com.greedy.zupzup.quiz.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.quiz.application.dto.QuizDto;
import com.greedy.zupzup.quiz.application.strategy.DefaultQuizGenerationStrategy;
import com.greedy.zupzup.quiz.application.strategy.EmptyQuizGenerationStrategy;
import com.greedy.zupzup.quiz.application.strategy.QuizGenerationStrategy;
import com.greedy.zupzup.quiz.exception.QuizException;
import com.greedy.zupzup.quiz.repository.QuizAttemptRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizGenerationService {

    private final MemberRepository memberRepository;
    private final LostItemRepository lostItemRepository;
    private final LostItemFeatureRepository lostItemFeatureRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    private final DefaultQuizGenerationStrategy defaultQuizGenerationStrategy;
    private final EmptyQuizGenerationStrategy emptyQuizGenerationStrategy;

    @Transactional(readOnly = true)
    public List<QuizDto> getLostItemQuizzes(Long lostItemId, Long memberId) {

        Member member = memberRepository.getById(memberId);
        LostItem lostItem = findAndValidateLostItem(lostItemId);

        validateQuizAttempt(lostItem, member);

        QuizGenerationStrategy strategy = lostItem.isEtcCategory()
                ? emptyQuizGenerationStrategy
                : defaultQuizGenerationStrategy;

        List<LostItemFeature> lostItemFeatures = lostItem.isEtcCategory()
                ? List.of()
                : lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(lostItem.getId());

        return strategy.createQuizzes(lostItemFeatures);
    }

    private void validateQuizAttempt(LostItem lostItem, Member member) {
        boolean hasIncorrectAttempt = quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(lostItem.getId(), member.getId());
        if (hasIncorrectAttempt) {
            throw new ApplicationException(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED);
        }
    }

    private LostItem findAndValidateLostItem(Long lostItemId) {
        LostItem lostItem = lostItemRepository.getWithCategoryById(lostItemId);

        if (!lostItem.isPledgeable()) {
            throw new ApplicationException(LostItemException.ACCESS_FORBIDDEN);
        }
        return lostItem;
    }
}
