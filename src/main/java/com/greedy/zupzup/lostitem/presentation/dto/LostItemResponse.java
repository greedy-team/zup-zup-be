package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;

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
    public static LostItemResponse from(LostItemListCommand command) {
        return new LostItemResponse(
                command.id(),
                command.categoryId(),
                command.categoryName(),
                command.categoryIconUrl(),
                command.schoolAreaId(),
                command.schoolAreaName(),
                command.findAreaDetail(),
                command.createdAt().toString()
        );
    }
}
