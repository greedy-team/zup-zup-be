package com.greedy.zupzup.alert.presentation.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubscriptionUpdateRequest(
        @NotNull(message = "카테고리 ID 목록은 필수입니다.")
        List<Long> categoryIds
) {
}
