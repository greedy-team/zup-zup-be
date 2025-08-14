package com.greedy.zupzup.lostitem.presentation.dto;

import org.springframework.data.domain.Page;

public record PageInfoResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasPrev,
        boolean hasNext
) {
    public static PageInfoResponse from(Page<?> page) {
        return new PageInfoResponse(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasPrevious(),
                page.hasNext()
        );
    }
}
