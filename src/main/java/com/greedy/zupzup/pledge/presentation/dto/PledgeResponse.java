package com.greedy.zupzup.pledge.presentation.dto;

import com.greedy.zupzup.pledge.domain.Pledge;

public record PledgeResponse(
        Long pledgeId,
        String message,
        String foundAreaDetail
) {
    public static PledgeResponse from(Pledge pledge) {
        return new PledgeResponse(
                pledge.getId(),
                "서약이 완료되었습니다.",
                pledge.getLostItem().getDepositArea()
        );
    }
}
