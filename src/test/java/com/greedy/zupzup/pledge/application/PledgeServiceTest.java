package com.greedy.zupzup.pledge.application;


import static com.greedy.zupzup.common.fixture.LostItemFixture.ALREADY_PLEDGED_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.LostItemFixture.NON_QUIZ_CATEGORY_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.LostItemFixture.PLEDGEABLE_ELECTRONIC_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.MemberFixture.MEMBER;
import static com.greedy.zupzup.common.fixture.QuizAttemptFixture.CORRECT_QUIZ_ATTEMPT;
import static com.greedy.zupzup.common.fixture.QuizAttemptFixture.INCORRECT_QUIZ_ATTEMPT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.pledge.domain.Pledge;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
import com.greedy.zupzup.quiz.exception.QuizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

class PledgeServiceTest extends ServiceUnitTest {

    @InjectMocks
    private PledgeService pledgeService;

    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_LOST_ITEM_ID = 1L;
    private static final Long ETC_CATEGORY_LOST_ITEM_ID = 2L;

    private Member member;
    private LostItem pledgeableLostItem;
    private LostItem nonQuizCategoryLostItem;

    @BeforeEach
    void setUp() {
        member = MEMBER();
        pledgeableLostItem = PLEDGEABLE_ELECTRONIC_LOST_ITEM();
        nonQuizCategoryLostItem = NON_QUIZ_CATEGORY_LOST_ITEM();
        ReflectionTestUtils.setField(member, "id", TEST_MEMBER_ID);
        ReflectionTestUtils.setField(pledgeableLostItem, "id", TEST_LOST_ITEM_ID);
        ReflectionTestUtils.setField(nonQuizCategoryLostItem, "id", ETC_CATEGORY_LOST_ITEM_ID);
    }

    @Test
    void 퀴즈를_통과한_경우_서약_작성에_성공한다() {

        // given
        QuizAttempt correctAttempt = CORRECT_QUIZ_ATTEMPT(member, pledgeableLostItem);

        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(quizAttemptRepository.getByLostItemIdAndMemberId(TEST_LOST_ITEM_ID, TEST_MEMBER_ID)).willReturn(correctAttempt);
        given(pledgeRepository.save(any(Pledge.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Pledge result = pledgeService.createPledge(TEST_LOST_ITEM_ID, TEST_MEMBER_ID);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getOwner()).isEqualTo(member);
            softly.assertThat(result.getLostItem()).isEqualTo(pledgeableLostItem);
            softly.assertThat(pledgeableLostItem.getStatus()).isEqualTo(LostItemStatus.PLEDGED);
        });
        then(pledgeRepository).should().save(any(Pledge.class));
    }

    @Test
    void 퀴즈가_없는_카테고리는_바로_서약_작성에_성공한다() {

        // given
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getById(ETC_CATEGORY_LOST_ITEM_ID)).willReturn(nonQuizCategoryLostItem);
        given(pledgeRepository.save(any(Pledge.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Pledge result = pledgeService.createPledge(ETC_CATEGORY_LOST_ITEM_ID, TEST_MEMBER_ID);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(nonQuizCategoryLostItem.getStatus()).isEqualTo(LostItemStatus.PLEDGED);
        });
        then(quizAttemptRepository).should(never()).getByLostItemIdAndMemberId(anyLong(), anyLong());
    }

    @Test
    void 이미_서약_작성된_분실물은_예외가_발생한다() {

        // given
        LostItem alreadyPledgedItem = ALREADY_PLEDGED_LOST_ITEM();
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(alreadyPledgedItem);

        // when & then
        assertThatThrownBy(() -> pledgeService.createPledge(TEST_LOST_ITEM_ID, TEST_MEMBER_ID))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(LostItemException.ALREADY_PLEDGED.getDetail());
    }

    @Test
    void 퀴즈를_틀린_경우_서약_작성하면_예외가_발생한다() {

        // given
        QuizAttempt incorrectAttempt = INCORRECT_QUIZ_ATTEMPT(member, pledgeableLostItem);
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(quizAttemptRepository.getByLostItemIdAndMemberId(TEST_LOST_ITEM_ID, TEST_MEMBER_ID)).willReturn(incorrectAttempt);

        // when & then
        assertThatThrownBy(() -> pledgeService.createPledge(TEST_LOST_ITEM_ID, TEST_MEMBER_ID))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED.getDetail());
    }

    @Test
    void 퀴즈를_풀지_않은_경우_서약_작성하면_예외가_발생한다() {

        // given
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(quizAttemptRepository.getByLostItemIdAndMemberId(TEST_LOST_ITEM_ID, TEST_MEMBER_ID))
                .willThrow(new ApplicationException(QuizException.QUIZ_NOT_PASSED));

        // when & then
        assertThatThrownBy(() -> pledgeService.createPledge(TEST_LOST_ITEM_ID, TEST_MEMBER_ID))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(QuizException.QUIZ_NOT_PASSED.getDetail());
    }
}
