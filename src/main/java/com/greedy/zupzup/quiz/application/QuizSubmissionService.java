package com.greedy.zupzup.quiz.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.quiz.application.dto.AnswerCommand;
import com.greedy.zupzup.quiz.application.dto.QuizResultDto;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
import com.greedy.zupzup.quiz.exception.QuizException;
import com.greedy.zupzup.quiz.repository.QuizAttemptRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizSubmissionService {

    private final LostItemRepository lostItemRepository;
    private final LostItemFeatureRepository lostItemFeatureRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public QuizResultDto submitQuizAnswers(Long lostItemId, Long memberId, List<AnswerCommand> answers) {
        validate(lostItemId, memberId);

        List<LostItemFeature> correctAnswers = lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(
                lostItemId);
        boolean isCorrect = checkAnswers(correctAnswers, answers);

        saveQuizAttempt(lostItemId, memberId, isCorrect);

        LostItem lostItem = lostItemRepository.getReferenceById(lostItemId);
        return isCorrect ? QuizResultDto.correct(lostItem) : QuizResultDto.incorrect();
    }

    private void validate(Long lostItemId, Long memberId) {
        if (!lostItemRepository.existsById(lostItemId)) {
            throw new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND);
        }
        if (quizAttemptRepository.existsByLostItemIdAndMemberId(lostItemId, memberId)) {
            throw new ApplicationException(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED);
        }
    }

    private void saveQuizAttempt(Long lostItemId, Long memberId, boolean isCorrect) {
        Member member = memberRepository.getReferenceById(memberId);
        LostItem lostItem = lostItemRepository.getReferenceById(lostItemId);
        QuizAttempt attempt = QuizAttempt.builder()
                .member(member)
                .lostItem(lostItem)
                .isCorrect(isCorrect)
                .build();
        quizAttemptRepository.save(attempt);
    }

    private boolean checkAnswers(List<LostItemFeature> correctAnswers, List<AnswerCommand> submittedAnswers) {
        if (correctAnswers.size() != submittedAnswers.size()) {
            return false;
        }

        Map<Long, Long> correctAnswerMap = correctAnswers.stream()
                .collect(Collectors.toMap(
                        answer -> answer.getFeature().getId(),
                        answer -> answer.getSelectedOption().getId()
                ));

        return submittedAnswers.stream()
                .allMatch(submitted ->
                        correctAnswerMap.getOrDefault(submitted.featureId(), -1L)
                                .equals(submitted.selectedOptionId())
                );
    }
}
