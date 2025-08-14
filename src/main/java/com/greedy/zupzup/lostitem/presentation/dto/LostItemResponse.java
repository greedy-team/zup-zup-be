package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public record LostItemResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String createdAt,
        String thumbnailUrl
) {
    public static LostItemResponse from(LostItemListCommand command, String representativeImageUrl) {

        String thumbnail = (command.categoryIconUrl() != null && !command.categoryIconUrl().isBlank())
                ? command.categoryIconUrl()
                : representativeImageUrl;

        String createdAt = command.createdAt()
                .atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return new LostItemResponse(
                command.id(),
                command.categoryId(),
                command.categoryName(),
                command.categoryIconUrl(),
                command.schoolAreaId(),
                command.schoolAreaName(),
                command.foundAreaDetail(),
                createdAt,
                thumbnail
        );
    }
}
