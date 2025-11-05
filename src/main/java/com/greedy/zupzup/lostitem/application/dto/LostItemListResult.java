package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.repository.LostItemListProjection;
import java.time.LocalDateTime;

public record LostItemListResult(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        LocalDateTime createdAt
) {
    public static LostItemListResult from(LostItemListProjection projection) {
        return new LostItemListResult(
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
