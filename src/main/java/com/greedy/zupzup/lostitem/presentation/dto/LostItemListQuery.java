package com.greedy.zupzup.lostitem.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record LostItemListQuery(
        Long categoryId,
        Long schoolAreaId,
        @Min(value = 1, message = "page는 1 이상이어야 합니다.") Integer page,
        @Min(value = 1, message = "limit는 1 이상이어야 합니다.")
        @Max(value = 50, message = "limit는 50 이하이어야 합니다.") Integer limit
) {
    public int safePage() {
        return (page == null || page < 1) ? 1 : page;
    }

    public int safeLimit() {
        int raw = (limit == null) ? 20 : limit;
        return Math.max(1, Math.min(50, raw));
    }
}
