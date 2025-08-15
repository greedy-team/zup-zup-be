package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;

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
    public static LostItemResponse of(LostItemListCommand command, String representativeImageUrl) {

        String thumbnail = (command.categoryIconUrl() != null && !command.categoryIconUrl().isBlank())
                ? command.categoryIconUrl()
                : representativeImageUrl;


        String createdAt = command.createdAt()
                .atZone(java.time.ZoneId.of("Asia/Seoul"))
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);

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
