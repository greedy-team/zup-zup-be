package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.domain.LostItem;

public record LostItemRegisterResponse(
        Long lostItemId,
        String message
) {
    public static LostItemRegisterResponse from(LostItem lostItem) {
        return new LostItemRegisterResponse(
                lostItem.getId(),
                "분실물 등록에 성공했습니다."
        );
    }
}
