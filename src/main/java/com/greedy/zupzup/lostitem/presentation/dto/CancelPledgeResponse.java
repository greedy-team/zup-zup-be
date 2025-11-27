package com.greedy.zupzup.lostitem.presentation.dto;

public record CancelPledgeResponse(
        Long lostItemId,
        String status,
        String message
) {}
