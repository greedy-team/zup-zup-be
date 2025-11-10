package com.greedy.zupzup.admin.lostitem.presentation.dto;

public record ApproveLostItemsResponse(
        int successfulCount,
        int totalRequestedCount,
        String message
) {
    public static ApproveLostItemsResponse of(int success, int total) {
        return new ApproveLostItemsResponse(
                success,
                total,
                String.format("%d건의 분실물이 승인되었습니다.", success)
        );
    }
}
