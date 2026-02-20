package com.greedy.zupzup.alert.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.greedy.zupzup.alert.application.dto.MailSendCommand;
import com.greedy.zupzup.alert.repository.AlertDigestProjection;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.global.util.DateTimeUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

class AlertSchedulerTest extends ServiceUnitTest {

    @InjectMocks
    private AlertScheduler alertScheduler;

    @Mock
    private EmailSender emailSender;

    @Nested
    @DisplayName("일일 다이제스트 발송")
    class SendDailyDigest {

        @Test
        void 발송할_데이터가_없으면_메일을_보내지_않아야_한다() {
            // given
            given(lostItemRepository.findDigestData(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(Collections.emptyList());

            // when
            alertScheduler.sendDailyDigest();

            // then
            then(emailSender).should(never()).sendEmail(any(MailSendCommand.class));
        }

        @Test
        void 발송할_데이터가_있으면_이메일별로_그룹화하여_메일을_발송해야_한다() {
            // given
            AlertDigestProjection projection1 = mock(AlertDigestProjection.class);
            given(projection1.getEmail()).willReturn("user1@example.com");
            given(projection1.getCategoryName()).willReturn("전자제품");
            given(projection1.getAreaName()).willReturn("AI센터");
            given(projection1.getCount()).willReturn(1L);

            AlertDigestProjection projection2 = mock(AlertDigestProjection.class);
            given(projection2.getEmail()).willReturn("user2@example.com");
            given(projection2.getCategoryName()).willReturn("지갑");
            given(projection2.getAreaName()).willReturn("광개토관");
            given(projection2.getCount()).willReturn(2L);

            given(lostItemRepository.findDigestData(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(List.of(projection1, projection2));

            // when
            alertScheduler.sendDailyDigest();

            // then
            then(emailSender).should(times(2)).sendEmail(any(MailSendCommand.class));
        }

        @Test
        void 한국시간_기준으로_정확한_조회_범위로_데이터를_조회해야_한다() {
            // given
            LocalDate fixedDate = LocalDate.of(2024, 1, 20);
            LocalDateTime expectedEnd = LocalDateTime.of(fixedDate, LocalTime.of(9, 0));
            LocalDateTime expectedStart = expectedEnd.minusDays(1);

            try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
                mockedLocalDate.when(() -> LocalDate.now(DateTimeUtil.KST_ZONE_ID))
                        .thenReturn(fixedDate);

                // when
                alertScheduler.sendDailyDigest();

                // then
                then(lostItemRepository).should().findDigestData(eq(expectedStart), eq(expectedEnd));
            }
        }
    }
}
