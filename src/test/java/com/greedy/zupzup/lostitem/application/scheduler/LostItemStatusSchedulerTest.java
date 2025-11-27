package com.greedy.zupzup.lostitem.application.scheduler;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import org.mockito.Spy;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class LostItemStatusSchedulerTest extends ServiceUnitTest {

    private LostItemStatusScheduler lostItemStatusScheduler;

    private Clock clock = Clock.fixed(
            Instant.parse("2025-11-15T01:00:00Z"),
            ZoneId.systemDefault()
    );

    @BeforeEach
    void setUp() {
        lostItemStatusScheduler = new LostItemStatusScheduler(lostItemRepository, clock);
    }

    @Test
    void 스케줄러_실행_시_7일이_지난_PLEDGED_아이템을_FOUND로_업데이트해야_한다() {

        // given
        LocalDateTime fixedNow = LocalDateTime.now(clock);
        LocalDateTime expectedCutoffDate = fixedNow.minusDays(7);

        given(lostItemRepository.updateExpiredPledgedItemsToFound(
                LostItemStatus.FOUND,
                LostItemStatus.PLEDGED,
                expectedCutoffDate,
                fixedNow
        )).willReturn(5);

        // when
        lostItemStatusScheduler.autoUpdatePledgedToFound();

        // then
        then(lostItemRepository).should(times(1)).updateExpiredPledgedItemsToFound(
                eq(LostItemStatus.FOUND),
                eq(LostItemStatus.PLEDGED),
                eq(expectedCutoffDate),
                eq(fixedNow)
        );
    }
}
