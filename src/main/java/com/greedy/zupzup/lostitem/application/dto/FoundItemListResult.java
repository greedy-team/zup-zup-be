package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.global.util.DateTimeUtil;
import com.greedy.zupzup.lostitem.repository.FoundItemProjection;

import java.time.OffsetDateTime;

public record FoundItemListResult(
        Long id,
        Long categoryId,
        String categoryName,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String representativeImageUrl,
        String description,
        OffsetDateTime createdAt,
        OffsetDateTime foundAt
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
                DateTimeUtil.toKstOffset(projection.getCreatedAt()),
                DateTimeUtil.toKstOffset(projection.getFoundAt())
        );
    }
}
