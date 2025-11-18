package com.greedy.zupzup.lostitem.application.scheduler;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

class LostItemStatusSchedulerTest extends ServiceUnitTest {

    @InjectMocks
    private LostItemStatusScheduler lostItemStatusScheduler;

    @Test
    void 스케줄러_실행_시_7일이_지난_PLEDGED_아이템을_FOUND로_업데이트해야_한다() {

        // given
        LocalDateTime fixedNow = LocalDateTime.of(2025, 11, 15, 1, 0, 0);
        LocalDateTime expectedCutoffDate = fixedNow.minusDays(7);

        // try 문으로 Mocking 된 LocalDateTime.now() 시간 해제
        try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
            mockedStatic.when(LocalDateTime::now).thenReturn(fixedNow);

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

}
