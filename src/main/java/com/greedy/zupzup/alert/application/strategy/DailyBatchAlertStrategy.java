package com.greedy.zupzup.alert.application.strategy;

import com.greedy.zupzup.alert.application.AlertService;
import com.greedy.zupzup.global.util.DateTimeUtil;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("batch")
@RequiredArgsConstructor
public class DailyBatchAlertStrategy implements AlertStrategy {

    private final AlertService alertService;
    private final LostItemRepository lostItemRepository;

    @Override
    public void sendAlert(List<Long> approvedIds) {
        // 배치 전략에서는 승인 시점에 발송하지 않음 — 스케줄러가 처리
    }

    @Scheduled(cron = "0 0 9 * * *", zone = DateTimeUtil.KST_ZONE_NAME)
    public void sendDailyDigest() {
        LocalDateTime end = LocalDateTime.now(DateTimeUtil.KST_ZONE_ID);
        LocalDateTime start = end.minusDays(1);

        List<Long> ids = lostItemRepository.findIdsByApprovedAtBetween(start, end);

        if (ids.isEmpty()) {
            log.info("[DailyBatchAlertStrategy] 발송할 데이터가 없습니다.");
            return;
        }

        alertService.sendDailyDigest(ids);
        log.info("[DailyBatchAlertStrategy] 일일 알림 발송 완료 (대상 분실물: {}건)", ids.size());
    }
}
