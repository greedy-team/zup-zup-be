package com.greedy.zupzup.admin.lostitem.presentation.dto;

public record RejectLostItemsResponse(
        int successfulCount,
        int totalRequestedCount,
        String message
) {
    public static RejectLostItemsResponse of(int success, int total) {
        return new RejectLostItemsResponse(
                success,
                total,
                String.format("%d건의 분실물이 영구 삭제되었습니다.", success)
        );
    }
}
