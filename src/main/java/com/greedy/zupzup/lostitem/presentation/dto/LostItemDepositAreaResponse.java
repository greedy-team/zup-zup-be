package com.greedy.zupzup.lostitem.presentation.dto;

public record LostItemDepositAreaResponse(
        String depositArea
) {
    public static LostItemDepositAreaResponse from(String depositArea) {
        return new LostItemDepositAreaResponse(depositArea);
    }
}
