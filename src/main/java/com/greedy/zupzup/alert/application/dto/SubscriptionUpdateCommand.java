package com.greedy.zupzup.alert.application.dto;

import java.util.List;

public record SubscriptionUpdateCommand(
        Long memberId,
        List<Long> categoryIds
) {
    public static SubscriptionUpdateCommand of(Long memberId, List<Long> categoryIds) {
        return new SubscriptionUpdateCommand(memberId, categoryIds);
    }
}
