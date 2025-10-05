package com.greedy.zupzup.quiz.application;

import static com.greedy.zupzup.common.fixture.LostItemFeatureFixture.ELECTRONIC_LOST_ITEM_FEATURES;
import static com.greedy.zupzup.common.fixture.LostItemFixture.ALREADY_PLEDGED_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.LostItemFixture.NON_QUIZ_CATEGORY_LOST_ITEM;
import static com.greedy.zupzup.common.fixture.LostItemFixture.PENDING_LOST_ITEM;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
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

    @Captor
    private ArgumentCaptor<List<LostItemFeature>> featuresCaptor;

    private Member member;
    private LostItem pledgeableLostItem;
    private LostItem nonQuizCategoryLostItem;

    @BeforeEach
    void setUp() {
        member = MEMBER();
        pledgeableLostItem = PLEDGEABLE_ELECTRONIC_LOST_ITEM();
        nonQuizCategoryLostItem = NON_QUIZ_CATEGORY_LOST_ITEM();
        setId(member, TEST_MEMBER_ID);
        setId(pledgeableLostItem, TEST_LOST_ITEM_ID);
        setId(nonQuizCategoryLostItem, ETC_CATEGORY_LOST_ITEM_ID);
    }

    @Test
    void 기타_카테고리가_아니면_Default전략에_위임하고_그_결과를_그대로_반환해야_한다() {
        // given
        List<LostItemFeature> features = ELECTRONIC_LOST_ITEM_FEATURES(pledgeableLostItem);

        LostItemFeature brandFeature = features.stream()
                .filter(feature -> feature.getFeature().getQuizQuestion().contains("브랜드"))
                .findFirst().orElseThrow();

        LostItemFeature colorFeature = features.stream()
                .filter(feature -> feature.getFeature().getQuizQuestion().contains("색상"))
                .findFirst().orElseThrow();

        List<OptionDto> brandOptions = brandFeature.getFeatureOptions().stream()
                .limit(4)
                .map(OptionDto::from)
                .toList();

        List<OptionDto> colorOptions = colorFeature.getFeatureOptions().stream()
                .limit(4)
                .map(OptionDto::from)
                .toList();

        List<QuizDto> stubbed = List.of(
                QuizDto.of(brandFeature, brandOptions),
                QuizDto.of(colorFeature, colorOptions)
        );

        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(TEST_LOST_ITEM_ID,
                TEST_MEMBER_ID))
                .willReturn(false);
        given(lostItemFeatureRepository.findWithFeatureAndOptionsByLostItemId(TEST_LOST_ITEM_ID))
                .willReturn(features);

        given(defaultQuizGenerationStrategy.createQuizzes(anyList())).willReturn(stubbed);

        // when
        List<QuizDto> result = quizGenerationService.getLostItemQuizzes(TEST_LOST_ITEM_ID, TEST_MEMBER_ID);

        // then
        assertThat(result).isEqualTo(stubbed);
        then(defaultQuizGenerationStrategy).should().createQuizzes(featuresCaptor.capture());
        assertThat(featuresCaptor.getValue()).isSameAs(features);
        then(emptyQuizGenerationStrategy).shouldHaveNoInteractions();
    }

    @Test
    void 기타_카테고리면_레포지토리_조회없이_empty전략에_빈리스트를_위임하고_빈리스트_반환해야_한다() {
        // given
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(ETC_CATEGORY_LOST_ITEM_ID)).willReturn(nonQuizCategoryLostItem);
        given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(ETC_CATEGORY_LOST_ITEM_ID,
                TEST_MEMBER_ID))
                .willReturn(false);
        given(emptyQuizGenerationStrategy.createQuizzes(List.of())).willReturn(List.of());

        // when
        List<QuizDto> result = quizGenerationService.getLostItemQuizzes(ETC_CATEGORY_LOST_ITEM_ID, TEST_MEMBER_ID);

        // then
        assertThat(result).isEmpty();
        then(lostItemFeatureRepository).should(never()).findWithFeatureAndOptionsByLostItemId(anyLong());
        then(defaultQuizGenerationStrategy).shouldHaveNoInteractions();
        then(emptyQuizGenerationStrategy).should().createQuizzes(List.of());
    }

    @Test
    void 이미_수령_신청된_분실물에_대해_퀴즈_생성을_요청하면_전략_호출없이_예외가_발생해야_한다() {
        // given
        LostItem alreadyPledgedLostItem = ALREADY_PLEDGED_LOST_ITEM();
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(TEST_LOST_ITEM_ID)).willReturn(alreadyPledgedLostItem);

        // when & then
        assertThatThrownBy(() -> quizGenerationService.getLostItemQuizzes(TEST_LOST_ITEM_ID, TEST_MEMBER_ID))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(LostItemException.ACCESS_FORBIDDEN.getDetail());

        then(quizAttemptRepository).should(never()).findByLostItem_IdAndMember_Id(anyLong(), anyLong());
        then(lostItemFeatureRepository).should(never()).findWithFeatureAndOptionsByLostItemId(anyLong());
        then(defaultQuizGenerationStrategy).shouldHaveNoInteractions();
        then(emptyQuizGenerationStrategy).shouldHaveNoInteractions();
    }

    @Test
    void 이미_퀴즈를_틀린_기록이_있는_경우_전략_호출없이_예외가_발생해야_한다() {
        // given
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(TEST_LOST_ITEM_ID)).willReturn(pledgeableLostItem);
        given(quizAttemptRepository.existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(anyLong(),
                anyLong())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> quizGenerationService.getLostItemQuizzes(TEST_LOST_ITEM_ID, TEST_MEMBER_ID))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED.getDetail());
        then(lostItemFeatureRepository).should(never()).findWithFeatureAndOptionsByLostItemId(anyLong());
        then(defaultQuizGenerationStrategy).shouldHaveNoInteractions();
        then(emptyQuizGenerationStrategy).shouldHaveNoInteractions();
    }

    @Test
    void 검토_중인_분실물에_대해_퀴즈_생성을_요청하면_전략_호출없이_예외가_발생해야_한다() {
        // given
        LostItem pendingLostItem = PENDING_LOST_ITEM();
        given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
        given(lostItemRepository.getWithCategoryById(TEST_LOST_ITEM_ID)).willReturn(pendingLostItem);

        // when & then
        assertThatThrownBy(() -> quizGenerationService.getLostItemQuizzes(TEST_LOST_ITEM_ID, TEST_MEMBER_ID))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(LostItemException.ACCESS_FORBIDDEN.getDetail());

        then(quizAttemptRepository).should(never()).existsByLostItem_IdAndMember_IdAndIsCorrectIsFalse(anyLong(), anyLong());
        then(lostItemFeatureRepository).should(never()).findWithFeatureAndOptionsByLostItemId(anyLong());
        then(defaultQuizGenerationStrategy).shouldHaveNoInteractions();
        then(emptyQuizGenerationStrategy).shouldHaveNoInteractions();
    }
}
