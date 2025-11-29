package com.greedy.zupzup.lostitem.presentation.dto;

public record CancelPledgeResponse(
        Long lostItemId,
        String status,
        String message
) {
    public static CancelPledgeResponse of(Long id, String status) {
        return new CancelPledgeResponse(id, status, "서약이 정상적으로 취소되었습니다.");
    }
}
