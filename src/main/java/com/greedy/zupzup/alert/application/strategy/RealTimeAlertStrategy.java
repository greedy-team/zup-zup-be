package com.greedy.zupzup.alert.application.strategy;

import com.greedy.zupzup.alert.application.AlertService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("realtime")
@RequiredArgsConstructor
public class RealTimeAlertStrategy implements AlertStrategy {

    private final AlertService alertService;

    @Override
    public void sendAlert(List<Long> approvedIds) {
        for (Long id : approvedIds) {
            alertService.sendApprovalAlert(id);
        }
    }
}
