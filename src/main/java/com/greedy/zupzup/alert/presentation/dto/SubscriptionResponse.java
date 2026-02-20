package com.greedy.zupzup.alert.presentation.dto;

import com.greedy.zupzup.alert.application.dto.SubscriptionResult;

import java.util.List;

public record SubscriptionResponse(
        List<Long> categoryIds
) {
    public static SubscriptionResponse from(SubscriptionResult result) {
        return new SubscriptionResponse(result.categoryIds());
    }
}
