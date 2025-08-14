package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.repository.LostItemListProjection;
import java.time.LocalDateTime;

public record LostItemListCommand(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        LocalDateTime createdAt
) {
    public static LostItemListCommand from(LostItemListProjection projection) {
        return new LostItemListCommand(
                projection.getId(),
                projection.getCategoryId(),
                projection.getCategoryName(),
                projection.getCategoryIconUrl(),
                projection.getSchoolAreaId(),
                projection.getSchoolAreaName(),
                projection.getFoundAreaDetail(),
                projection.getCreatedAt()
        );
    }
}
