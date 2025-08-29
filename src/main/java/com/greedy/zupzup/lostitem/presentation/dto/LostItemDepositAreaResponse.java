package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;

public record LostItemDepositAreaResponse(
        String depositArea
) {
    public static LostItemDepositAreaResponse from(String depositArea) {
        return new LostItemDepositAreaResponse(depositArea);
    }
}
