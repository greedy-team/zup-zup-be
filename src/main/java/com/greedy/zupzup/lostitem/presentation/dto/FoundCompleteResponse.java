package com.greedy.zupzup.lostitem.presentation.dto;

public record FoundCompleteResponse(
        Long lostItemId,
        String status,
        String message
) {
    public static FoundCompleteResponse of(Long id, String status) {
        return new FoundCompleteResponse(id, status, "습득 완료되었습니다.");
    }
}
