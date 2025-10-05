package com.greedy.zupzup.quiz.application;

import static com.greedy.zupzup.common.fixture.LostItemFeatureFixture.ELECTRONIC_LOST_ITEM_FEATURES;
import static com.greedy.zupzup.common.fixture.LostItemFixture.ALREADY_PLEDGED_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.LostItemFixture.PENDING_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.LostItemFixture.PLEDGEABLE_ELECTRONIC_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.MemberFixture.MEMBER;
import static com.greedy.zupzup.common.fixture.QuizAttemptFixture.INCORRECT_QUIZ_ATTEMPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.quiz.application.dto.AnswerCommand;
import com.greedy.zupzup.quiz.application.dto.QuizResultDto;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
import com.greedy.zupzup.quiz.exception.QuizException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

class QuizSubmissionServiceTest extends ServiceUnitTest {

    @InjectMocks
    private QuizSubmissionService quizSubmissionService;

    private static final Long TEST_LOST_ITEM_ID = 1L;
    private static final Long TEST_MEMBER_ID = 1L;

    private Member member;
    private LostItem pledgeableLostItem;

    @BeforeEach
    void setUp() {
        member = MEMBER();
        pledgeableLostItem = PLEDGEABLE_ELECTRONIC_LOST_ITEM();
        setId(member, TEST_MEMBER_ID);
        setId(pledgeableLostItem, TEST_LOST_ITEM_ID);

    }

    @Test
    void 정답을_제출하면_올바른_결과가_반환되어야_한다() {
        // given
        List<LostItemFeature> correctFeatures = ELECTRONIC_LOST_ITEM_FEATURES(pledgeableLostItem);
        List<AnswerCommand> correctAnswers = correctFeatures.stream()
                .map(feature -> new AnswerCommand(feature.getFeatureId(), feature.getSelectedOptionId()))
                .collect(Collectors.toList());

        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(quizAttemptRepository.findByLostItem_IdAndMember_Id(anyLong(), anyLong())).willReturn(Optional.empty());
        given(lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(TEST_LOST_ITEM_ID)).willReturn(correctFeatures);

        // when
        QuizResultDto result = quizSubmissionService.submitQuizAnswers(TEST_LOST_ITEM_ID, TEST_MEMBER_ID, correctAnswers);

        // then
        assertThat(result.correct()).isTrue();
        then(quizAttemptRepository).should().save(any(QuizAttempt.class));
    }

    @Test
    void 오답을_제출하면_오답_결과가_반환되어야_한다() {
        // given
        List<LostItemFeature> correctFeatures = ELECTRONIC_LOST_ITEM_FEATURES(pledgeableLostItem);
        long wrongOptionId = 99L;
        List<AnswerCommand> incorrectAnswers = correctFeatures.stream()
                .map(feature -> new AnswerCommand(feature.getFeatureId(), wrongOptionId))
                .collect(Collectors.toList());

        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(quizAttemptRepository.findByLostItem_IdAndMember_Id(anyLong(), anyLong())).willReturn(Optional.empty());
        given(lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(TEST_LOST_ITEM_ID)).willReturn(correctFeatures);

        // when
        QuizResultDto result = quizSubmissionService.submitQuizAnswers(TEST_LOST_ITEM_ID, TEST_MEMBER_ID, incorrectAnswers);

        // then
        assertThat(result.correct()).isFalse();
        then(quizAttemptRepository).should().save(any(QuizAttempt.class));
    }

    @Test
    void 질문_개수가_맞지_않으면_오답_결과가_반환되어야_한다() {
        // given
        List<LostItemFeature> correctFeatures = ELECTRONIC_LOST_ITEM_FEATURES(pledgeableLostItem);
        List<AnswerCommand> insufficientAnswers = List.of();

        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(quizAttemptRepository.findByLostItem_IdAndMember_Id(anyLong(), anyLong())).willReturn(Optional.empty());
        given(lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(TEST_LOST_ITEM_ID)).willReturn(correctFeatures);

        // when
        QuizResultDto result = quizSubmissionService.submitQuizAnswers(TEST_LOST_ITEM_ID, TEST_MEMBER_ID, insufficientAnswers);

        // then
        assertThat(result.correct()).isFalse();
        then(quizAttemptRepository).should().save(any(QuizAttempt.class));
    }

    @Test
    void 이미_수령_신청된_분실물에_대해_퀴즈를_제출하면_예외가_발생해야_한다() {
        // given
        LostItem alreadyPledgedLostItem = ALREADY_PLEDGED_LOST_ITEM();
        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(alreadyPledgedLostItem);
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);

        // when & then
        assertThatThrownBy(() -> quizSubmissionService.submitQuizAnswers(TEST_LOST_ITEM_ID, TEST_MEMBER_ID, List.of()))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(LostItemException.ACCESS_FORBIDDEN.getDetail());

        then(quizAttemptRepository).should(never()).save(any(QuizAttempt.class));
    }

    @Test
    void 이미_퀴즈를_틀린_기록이_있는_경우_예외가_발생해야_한다() {
        // given
        QuizAttempt incorrectQuizAttempt = INCORRECT_QUIZ_ATTEMPT(member, pledgeableLostItem);

        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(quizAttemptRepository.findByLostItem_IdAndMember_Id(anyLong(), anyLong())).willReturn(Optional.of(incorrectQuizAttempt));

        // when & then
        assertThatThrownBy(() -> quizSubmissionService.submitQuizAnswers(TEST_LOST_ITEM_ID, TEST_MEMBER_ID, List.of()))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED.getDetail());

        then(quizAttemptRepository).should(never()).save(any(QuizAttempt.class));
    }

    @Test
    void 검토_중인_분실물에_대해_퀴즈를_제출하면_예외가_발생해야_한다() {
        // given
        LostItem pendingLostItem = PENDING_LOST_ITEM();
        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(pendingLostItem);
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);

        // when & then
        assertThatThrownBy(() -> quizSubmissionService.submitQuizAnswers(TEST_LOST_ITEM_ID, TEST_MEMBER_ID, List.of()))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(LostItemException.ACCESS_FORBIDDEN.getDetail());

        then(quizAttemptRepository).should(never()).findByLostItem_IdAndMember_Id(anyLong(), anyLong());
        then(quizAttemptRepository).should(never()).save(any(QuizAttempt.class));
    }
}
