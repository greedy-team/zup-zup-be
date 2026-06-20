package com.greedy.zupzup.admin.lostitem.presentation.dto;

public record UpdateLostItemResponse(
        Long lostItemId,
        String message
) {
    public static UpdateLostItemResponse from(Long id) {
        return new UpdateLostItemResponse(id, "분실물 정보가 성공적으로 수정 및 승인되었습니다.");
    }
}
