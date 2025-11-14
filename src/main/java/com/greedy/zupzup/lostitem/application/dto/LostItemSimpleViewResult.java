package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.domain.LostItem;
import java.time.LocalDateTime;

public record LostItemSimpleViewResult(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        LocalDateTime createdAt,
        String representativeImageUrl
) {

    public static LostItemSimpleViewResult of(LostItem item, String representativeImageUrl) {
        return new LostItemSimpleViewResult(
                item.getId(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getCategory().getIconUrl(),
                item.getFoundArea().getId(),
                item.getFoundArea().getAreaName(),
                item.getFoundAreaDetail(),
                item.getCreatedAt(),
                representativeImageUrl
        );
    }
}
