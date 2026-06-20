package com.greedy.zupzup.alert.application;

import static com.greedy.zupzup.common.fixture.CategoryFixture.ELECTRONIC;
import static com.greedy.zupzup.common.fixture.CategoryFixture.WALLET;
import static com.greedy.zupzup.common.fixture.KeywordAlertFixture.KEYWORD_ALERT;
import static com.greedy.zupzup.common.fixture.MemberFixture.MEMBER_WITH_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.greedy.zupzup.alert.application.dto.MailSendCommand;
import com.greedy.zupzup.alert.application.dto.SubscriptionResult;
import com.greedy.zupzup.alert.application.dto.SubscriptionUpdateCommand;
import com.greedy.zupzup.alert.domain.KeywordAlert;
import com.greedy.zupzup.alert.repository.AlertDigestProjection;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.category.domain.Category;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class AlertServiceTest extends ServiceUnitTest {

    @InjectMocks
    private AlertService alertService;

    @Mock
    private EmailSender emailSender;

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
    @DisplayName("실시간 승인 알림 발송")
    class SendApprovalAlert {

        @Test
        void 알림_대상자가_없으면_메일을_보내지_않아야_한다() {
            // given
            LostItem lostItem = mock(LostItem.class);
            given(lostItem.getCreatedAt()).willReturn(LocalDateTime.now());
            given(lostItemRepository.getById(1L)).willReturn(lostItem);
            given(lostItemRepository.findDigestDataByIds(List.of(1L))).willReturn(List.of());

            // when
            alertService.sendApprovalAlert(1L);

            // then
            then(emailSender).should(never()).sendEmail(any(MailSendCommand.class));
        }

        @Test
        void 알림_대상자가_있으면_이메일별로_메일을_발송해야_한다() {
            // given
            LostItem lostItem = mock(LostItem.class);
            given(lostItem.getCreatedAt()).willReturn(LocalDateTime.of(2025, 1, 19, 18, 32));

            AlertDigestProjection projection1 = mock(AlertDigestProjection.class);
            given(projection1.getEmail()).willReturn("user1@example.com");
            given(projection1.getCategoryName()).willReturn("전자기기");
            given(projection1.getCategoryEmoji()).willReturn("📱");
            given(projection1.getAreaName()).willReturn("광개토관");
            given(projection1.getCount()).willReturn(1L);

            AlertDigestProjection projection2 = mock(AlertDigestProjection.class);
            given(projection2.getEmail()).willReturn("user2@example.com");
            given(projection2.getCategoryName()).willReturn("지갑");
            given(projection2.getCategoryEmoji()).willReturn("👜");
            given(projection2.getAreaName()).willReturn("AI센터");
            given(projection2.getCount()).willReturn(1L);

            given(lostItemRepository.getById(1L)).willReturn(lostItem);
            given(lostItemRepository.findDigestDataByIds(List.of(1L))).willReturn(List.of(projection1, projection2));

            // when
            alertService.sendApprovalAlert(1L);

            // then
            then(emailSender).should(times(2)).sendEmail(any(MailSendCommand.class));
        }
    }

    @Nested
    @DisplayName("일일 다이제스트 알림 발송")
    class SendDailyDigest {

        @Test
        void 발송할_데이터가_없으면_메일을_보내지_않아야_한다() {
            // given
            given(lostItemRepository.findDigestDataByIds(List.of(1L, 2L))).willReturn(List.of());

            // when
            alertService.sendDailyDigest(List.of(1L, 2L));

            // then
            then(emailSender).should(never()).sendEmail(any(MailSendCommand.class));
        }

        @Test
        void 발송할_데이터가_있으면_이메일별로_그룹화하여_메일을_발송해야_한다() {
            // given
            AlertDigestProjection projection1 = mock(AlertDigestProjection.class);
            given(projection1.getEmail()).willReturn("user1@example.com");
            given(projection1.getCategoryName()).willReturn("전자기기");
            given(projection1.getCategoryEmoji()).willReturn("📱");
            given(projection1.getAreaName()).willReturn("AI센터");
            given(projection1.getCount()).willReturn(1L);

            AlertDigestProjection projection2 = mock(AlertDigestProjection.class);
            given(projection2.getEmail()).willReturn("user2@example.com");
            given(projection2.getCategoryName()).willReturn("지갑");
            given(projection2.getCategoryEmoji()).willReturn("👜");
            given(projection2.getAreaName()).willReturn("광개토관");
            given(projection2.getCount()).willReturn(2L);

            given(lostItemRepository.findDigestDataByIds(List.of(1L, 2L))).willReturn(List.of(projection1, projection2));

            // when
            alertService.sendDailyDigest(List.of(1L, 2L));

            // then
            then(emailSender).should(times(2)).sendEmail(any(MailSendCommand.class));
        }
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
