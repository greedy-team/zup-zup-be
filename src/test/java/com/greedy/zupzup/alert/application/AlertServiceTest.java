package com.greedy.zupzup.alert.application;

import static com.greedy.zupzup.common.fixture.CategoryFixture.ELECTRONIC;
import static com.greedy.zupzup.common.fixture.CategoryFixture.WALLET;
import static com.greedy.zupzup.common.fixture.KeywordAlertFixture.KEYWORD_ALERT;
import static com.greedy.zupzup.common.fixture.MemberFixture.MEMBER_WITH_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.greedy.zupzup.alert.application.dto.SubscriptionResult;
import com.greedy.zupzup.alert.application.dto.SubscriptionUpdateCommand;
import com.greedy.zupzup.alert.domain.KeywordAlert;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.category.domain.Category;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class AlertServiceTest extends ServiceUnitTest {

    @InjectMocks
    private AlertService alertService;

    private static final Long TEST_MEMBER_ID = 1L;
    private static final Long TEST_CATEGORY_ID_1 = 1L;
    private static final Long TEST_CATEGORY_ID_2 = 2L;

    private Member member;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        member = MEMBER_WITH_EMAIL();
        category1 = ELECTRONIC();
        category2 = WALLET();
        setId(member, TEST_MEMBER_ID);
        setId(category1, TEST_CATEGORY_ID_1);
        setId(category2, TEST_CATEGORY_ID_2);
    }

    @Nested
    @DisplayName("구독 목록 조회")
    class GetSubscriptions {

        @Test
        void 회원의_구독_목록을_조회하면_카테고리_ID_리스트가_반환되어야_한다() {
            // given
            KeywordAlert alert1 = KEYWORD_ALERT(member, category1);
            KeywordAlert alert2 = KEYWORD_ALERT(member, category2);
            given(keywordAlertRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(List.of(alert1, alert2));

            // when
            SubscriptionResult result = alertService.getSubscriptions(TEST_MEMBER_ID);

            // then
            assertThat(result.categoryIds()).hasSize(2);
            assertThat(result.categoryIds()).containsExactlyInAnyOrder(TEST_CATEGORY_ID_1, TEST_CATEGORY_ID_2);
        }
    }

    @Nested
    @DisplayName("구독 목록 수정")
    class UpdateSubscriptions {

        @Test
        void 구독_목록을_수정하면_기존_구독을_삭제하고_새로운_구독을_저장해야_한다() {
            // given
            List<Long> newCategoryIds = List.of(TEST_CATEGORY_ID_2);
            SubscriptionUpdateCommand command = new SubscriptionUpdateCommand(TEST_MEMBER_ID, newCategoryIds);

            KeywordAlert existingAlert = KEYWORD_ALERT(member, category1);
            given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
            given(keywordAlertRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(List.of(existingAlert));
            given(categoryRepository.getAllByIds(anyList())).willReturn(List.of(category2));

            // when
            alertService.updateSubscriptions(command);

            // then
            then(keywordAlertRepository).should().deleteAll(anyList());
            then(keywordAlertRepository).should().saveAll(anyList());
        }

        @Test
        void 빈_목록으로_수정하면_기존_구독만_삭제하고_저장은_하지_않아야_한다() {
            // given
            List<Long> emptyCategoryIds = List.of();
            SubscriptionUpdateCommand command = new SubscriptionUpdateCommand(TEST_MEMBER_ID, emptyCategoryIds);

            KeywordAlert existingAlert = KEYWORD_ALERT(member, category1);
            given(memberRepository.getById(TEST_MEMBER_ID)).willReturn(member);
            given(keywordAlertRepository.findAllByMemberId(TEST_MEMBER_ID)).willReturn(List.of(existingAlert));

            // when
            alertService.updateSubscriptions(command);

            // then
            then(keywordAlertRepository).should().deleteAll(anyList());
            then(keywordAlertRepository).should(never()).saveAll(anyList());
        }
    }
}
