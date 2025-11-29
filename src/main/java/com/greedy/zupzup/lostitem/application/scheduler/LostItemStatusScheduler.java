package com.greedy.zupzup.lostitem.application.scheduler;

import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LostItemStatusScheduler {

    private final LostItemRepository lostItemRepository;
    private final Clock clock;

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void autoUpdatePledgedToFound() {
        log.info("[Scheduler] 7일 경과 분실물 'PLEDGED' -> 'FOUND' 처리 시작...");

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime cutoffDate = now.minusDays(7);

        int updatedCount = lostItemRepository.updateExpiredPledgedItemsToFound(
                LostItemStatus.FOUND,
                LostItemStatus.PLEDGED,
                cutoffDate,
                now
        );

        if (updatedCount > 0) {
            log.info("[Scheduler] 총 {}개의 분실물 상태가 'FOUND'로 변경됨.", updatedCount);
        } else {
            log.info("[Scheduler] 상태 변경 대상 서약 분실물 없음.");
        }
    }
}
