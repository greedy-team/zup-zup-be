package com.greedy.zupzup.quiz.application;

import static com.greedy.zupzup.common.fixture.LostItemFeatureFixture.ELECTRONIC_LOST_ITEM_FEATURES;
import static com.greedy.zupzup.common.fixture.LostItemFixture.ALREADY_PLEDGED_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.LostItemFixture.NON_QUIZ_CATEGORY_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.LostItemFixture.PLEDGEABLE_ELECTRONIC_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.MemberFixture.MEMBER;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.quiz.application.dto.OptionDto;
import com.greedy.zupzup.quiz.application.dto.QuizDto;
import com.greedy.zupzup.quiz.exception.QuizException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

class QuizGenerationServiceTest extends ServiceUnitTest {

    @InjectMocks
    private QuizGenerationService quizGenerationService;

    private static final Long TEST_LOST_ITEM_ID = 1L;
    private static final Long ETC_CATEGORY_LOST_ITEM_ID = 2L;
    private static final Long TEST_MEMBER_ID = 1L;
    private static final int QUIZ_OPTIONS_COUNT = 4;
    private static final String ETC_OPTION_TEXT = "기타";

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
    void 퀴즈_생성에_성공하면_생성된_퀴즈_리스트를_반환해야_한다() {

        // given
        List<LostItemFeature> mockFeatures = ELECTRONIC_LOST_ITEM_FEATURES(pledgeableLostItem);

        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(anyLong(), anyLong())).willReturn(false);
        given(lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(TEST_LOST_ITEM_ID)).willReturn(
                mockFeatures);

        // when
        List<QuizDto> result = quizGenerationService.getLostItemQuizzes(TEST_LOST_ITEM_ID, TEST_MEMBER_ID);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).isNotNull();
            softly.assertThat(result).hasSize(2);
            QuizDto brandQuiz = result.stream().filter(q -> q.question().contains("브랜드")).findFirst().orElseThrow();
            QuizDto colorQuiz = result.stream().filter(q -> q.question().contains("색상")).findFirst().orElseThrow();
            softly.assertThat(brandQuiz.options()).hasSize(4);
            softly.assertThat(colorQuiz.options()).hasSize(4);
        });

        then(memberRepository).should().getById(TEST_MEMBER_ID);
        then(lostItemRepository).should().getWithCategoryById(TEST_LOST_ITEM_ID);
        then(quizAttemptRepository).should().existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(anyLong(), anyLong());
        then(lostItemFeatureRepository).should().findWithFeatureAndOptionsByLostItemId(TEST_LOST_ITEM_ID);
    }

    @Test
    void 이미_수령_신청된_분실물에_대해_퀴즈_생성을_요청하면_예외가_발생해야_한다() {

        // given
        LostItem alreadyPledgedLostItem = ALREADY_PLEDGED_LOST_ITEM();
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(TEST_LOST_ITEM_ID)).willReturn(alreadyPledgedLostItem);

        // when & then
        assertThatThrownBy(() -> quizGenerationService.getLostItemQuizzes(TEST_LOST_ITEM_ID, TEST_MEMBER_ID))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(LostItemException.ALREADY_PLEDGED.getDetail());

        then(quizAttemptRepository).should(never()).findByLostItem_IdAndMember_Id(anyLong(), anyLong());
        then(lostItemFeatureRepository).should(never()).findWithFeatureAndOptionsByLostItemId(anyLong());
    }

    @Test
    void 이미_퀴즈를_틀린_기록이_있는_경우_예외가_발생해야_한다() {

        // given
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(anyLong(), anyLong())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> quizGenerationService.getLostItemQuizzes(TEST_LOST_ITEM_ID, TEST_MEMBER_ID))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED.getDetail());
        then(lostItemFeatureRepository).should(never()).findWithFeatureAndOptionsByLostItemId(anyLong());
    }

    @Test
    void 퀴즈_생성_대상이_아닌_카테고리인_경우_빈_리스트를_반환해야_한다() {

        // given
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(ETC_CATEGORY_LOST_ITEM_ID)).willReturn(nonQuizCategoryLostItem);
        given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(anyLong(), anyLong())).willReturn(false);

        // when
        List<QuizDto> result = quizGenerationService.getLostItemQuizzes(ETC_CATEGORY_LOST_ITEM_ID, TEST_MEMBER_ID);

        // then
        assertThat(result).isEmpty();
        then(lostItemFeatureRepository).should(never()).findWithFeatureAndOptionsByLostItemId(anyLong());
    }

    @Test
    void 기타_옵션이_존재하는_경우_항상_마지막에_위치해야_한다() {

        // given
        List<LostItemFeature> mockFeatures = ELECTRONIC_LOST_ITEM_FEATURES(pledgeableLostItem);

        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(anyLong(), anyLong())).willReturn(false);
        given(lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(TEST_LOST_ITEM_ID)).willReturn(
                mockFeatures);

        // when
        List<QuizDto> result = quizGenerationService.getLostItemQuizzes(TEST_LOST_ITEM_ID, TEST_MEMBER_ID);

        // then
        List<String> optionValues = result.get(0).options().stream()
                .map(OptionDto::text)
                .collect(Collectors.toList());

        assertThat(optionValues).hasSize(QUIZ_OPTIONS_COUNT);
        assertThat(optionValues.get(QUIZ_OPTIONS_COUNT - 1)).isEqualTo(ETC_OPTION_TEXT);
    }
}
