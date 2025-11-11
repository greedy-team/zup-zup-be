package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.repository.FoundItemProjection;

import java.time.LocalDateTime;

public record FoundItemListResult(
        Long id,
        Long categoryId,
        String categoryName,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String representativeImageUrl,
        String description,
        LocalDateTime createdAt,
        LocalDateTime foundAt
) {
    public static FoundItemListResult from(FoundItemProjection projection) {
        return new FoundItemListResult(
                projection.getId(),
                projection.getCategoryId(),
                projection.getCategoryName(),
                projection.getSchoolAreaId(),
                projection.getSchoolAreaName(),
                projection.getFoundAreaDetail(),
                projection.getRepresentativeImageUrl(),
                projection.getDescription(),
                projection.getCreatedAt(),
                projection.getFoundAt()
        );
    }
}
