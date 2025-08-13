package com.greedy.zupzup.lostitem.presentation.dto;

public record PageInfoResponse(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasPrev,
        boolean hasNext
) {
    public static PageInfoResponse of(int page, int size, long totalElements, int totalPages, boolean hasPrev, boolean hasNext) {
        return new PageInfoResponse(page, size, totalElements, totalPages, hasPrev, hasNext);
    }
}
