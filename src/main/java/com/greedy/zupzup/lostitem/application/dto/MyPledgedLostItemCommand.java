package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.repository.MyPledgedLostItemProjection;
import java.time.LocalDateTime;

public record MyPledgedLostItemCommand(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        LocalDateTime createdAt,
        String representativeImageUrl,
        LocalDateTime pledgedAt,
        String depositArea
) {
    public static MyPledgedLostItemCommand from(MyPledgedLostItemProjection projection) {
        return new MyPledgedLostItemCommand(
                projection.getId(),
                projection.getCategoryId(),
                projection.getCategoryName(),
                null,
                projection.getSchoolAreaId(),
                projection.getSchoolAreaName(),
                projection.getFoundAreaDetail(),
                projection.getCreatedAt(),
                projection.getRepresentativeImageUrl(),
                projection.getPledgedAt(),
                projection.getDepositArea()
        );
    }
}
