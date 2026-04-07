package com.greedy.zupzup.alert.application.strategy;

import java.util.List;

public interface AlertStrategy {

    void sendAlert(List<Long> approvedIds);
}
