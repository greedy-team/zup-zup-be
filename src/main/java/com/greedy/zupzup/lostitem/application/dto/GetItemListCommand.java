package com.greedy.zupzup.lostitem.application.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record GetItemListCommand(
        Long categoryId,
        Long schoolAreaId,
        Pageable pageable
) {
    public static final int PAGE_NUMBER_OFFSET = 1;

    public static GetItemListCommand of(Long categoryId, Long schoolAreaId, Integer page, Integer limit) {
        return new GetItemListCommand(
                categoryId,
                schoolAreaId,
                PageRequest.of(page - PAGE_NUMBER_OFFSET, limit)
        );
    }
}
