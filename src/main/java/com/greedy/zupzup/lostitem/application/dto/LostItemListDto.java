package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.repository.LostItemListProjection;
import java.time.LocalDateTime;

public record LostItemListDto(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String findArea,
        LocalDateTime createdAt
) {
    public static LostItemListDto from(LostItemListProjection p) {
        return new LostItemListDto(
                p.getId(),
                p.getCategoryId(),
                p.getCategoryName(),
                p.getCategoryIconUrl(),
                p.getSchoolAreaId(),
                p.getSchoolAreaName(),
                p.getFindArea(),
                p.getCreatedAt()
        );
    }
}
