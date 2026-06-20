package com.greedy.zupzup.admin.lostitem.application.dto;

public record UpdateLostItemResult(
        Long lostItemId,
        String message
) {
    public static UpdateLostItemResult from(Long id) {
        return new UpdateLostItemResult(
                id,
                "분실물 정보가 성공적으로 수정 및 승인되었습니다."
        );
    }
}
