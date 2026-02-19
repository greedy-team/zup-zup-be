package com.greedy.zupzup.alert.application.dto;

import java.util.List;

public record SubscriptionResult(
        List<Long> categoryIds
) {
    public static SubscriptionResult of(List<Long> categoryIds) {
        return new SubscriptionResult(categoryIds);
    }
}
