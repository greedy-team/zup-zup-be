package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record LostItemImageResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String description,
        List<String> imageUrls,
        String createdAt
) {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static LostItemImageResponse from(LostItemDetailViewCommand command) {
        return new LostItemImageResponse(
                command.id(),
                command.categoryId(),
                command.categoryName(),
                command.categoryIconUrl(),
                command.schoolAreaId(),
                command.schoolAreaName(),
                command.locationDetail(),
                command.description(),
                command.imageUrls(),
                command.createdAt().atZone(KST).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        );
    }
}
