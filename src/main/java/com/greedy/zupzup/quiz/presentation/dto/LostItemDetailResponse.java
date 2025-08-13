package com.greedy.zupzup.quiz.presentation.dto;

import com.greedy.zupzup.quiz.application.dto.LostItemDetailDto;

public record LostItemDetailResponse(
        String imageUrl,
        String description
) {

    public static LostItemDetailResponse from(LostItemDetailDto detailDto) {
        return new LostItemDetailResponse(detailDto.imageUrl(), detailDto.description());
    }
}
