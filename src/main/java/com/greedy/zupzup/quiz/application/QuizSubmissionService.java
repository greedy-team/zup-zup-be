package com.greedy.zupzup.quiz.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.exception.MemberException;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.pledge.exception.PledgeException;
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

    private static final long INVALID_OPTION_ID = -1L;

    @Transactional
    public QuizResultDto submitQuizAnswers(Long lostItemId, Long memberId, List<AnswerCommand> answers) {

        LostItem lostItem = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplicationException(MemberException.MEMBER_NOT_FOUND));

        validateSubmissionPossibility(lostItem, member);

        List<LostItemFeature> correctFeatures = lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(
                lostItemId);
        Map<Long, Long> correctAnswerMap = createCorrectAnswerMap(correctFeatures);
        boolean isCorrect = checkAnswers(correctAnswerMap, answers, correctFeatures.size());

        saveQuizAttempt(lostItem, member, isCorrect);

        return isCorrect ? QuizResultDto.correct(lostItem) : QuizResultDto.incorrect();
    }

    private void validateSubmissionPossibility(LostItem lostItem, Member member) {
        if (!lostItem.isPledgeable()) {
            throw new ApplicationException(LostItemException.ALREADY_PLEDGED);
        }

        quizAttemptRepository.findByLostItemIdAndMemberId(lostItem.getId(), member.getId())
                .ifPresent(attempt -> {
                    if (!attempt.getIsCorrect()) {
                        throw new ApplicationException(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED);
                    }
                });
    }

    private void saveQuizAttempt(LostItem lostItem, Member member, boolean isCorrect) {
        QuizAttempt attempt = QuizAttempt.builder()
                .member(member)
                .lostItem(lostItem)
                .isCorrect(isCorrect)
                .build();
        quizAttemptRepository.save(attempt);
    }

    private Map<Long, Long> createCorrectAnswerMap(List<LostItemFeature> correctFeatures) {
        return correctFeatures.stream()
                .collect(Collectors.toMap(
                        LostItemFeature::getFeatureId,
                        LostItemFeature::getSelectedOptionId
                ));
    }

    private boolean checkAnswers(Map<Long, Long> correctAnswerMap, List<AnswerCommand> submittedAnswers,
            int questionCount) {
        if (questionCount != submittedAnswers.size()) {
            return false;
        }
        return submittedAnswers.stream()
                .allMatch(submitted ->
                        correctAnswerMap.getOrDefault(submitted.featureId(), INVALID_OPTION_ID)
                                .equals(submitted.selectedOptionId())
                );
    }
}
