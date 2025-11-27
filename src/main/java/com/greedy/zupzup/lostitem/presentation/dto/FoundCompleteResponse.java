package com.greedy.zupzup.lostitem.presentation.dto;

public record FoundCompleteResponse(
        Long lostItemId,
        String status,
        String message
) {}
