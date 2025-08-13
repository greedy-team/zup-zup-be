package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemListDto;

public record LostItemResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String findArea,
        String createdAt
) {
    public static LostItemResponse from(LostItemListDto dto) {
        return new LostItemResponse(
                dto.id(),
                dto.categoryId(),
                dto.categoryName(),
                dto.categoryIconUrl(),
                dto.schoolAreaId(),
                dto.schoolAreaName(),
                dto.findArea(),
                dto.createdAt().toString()
        );
    }
}
