package com.greedy.zupzup.lostitem.presentation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record LostItemListQuery(
        Long categoryId,
        Long schoolAreaId,
        @Min(value = MIN_PAGE, message = "page는 1 이상이어야 합니다.") Integer page,
        @Min(value = MIN_LIMIT, message = "limit는 1 이상이어야 합니다.")
        @Max(value = MAX_LIMIT, message = "limit는 50 이하이어야 합니다.") Integer limit
) {
    public static final int MIN_PAGE = 1;

    public static final int DEFAULT_LIMIT = 20;
    public static final int MIN_LIMIT = 1;
    public static final int MAX_LIMIT = 50;

    public int safePage() {
        return (page == null || page < MIN_PAGE) ? MIN_PAGE : page;
    }

    public int safeLimit() {
        int raw = (limit == null) ? DEFAULT_LIMIT : limit;
        return Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, raw));
    }
}
