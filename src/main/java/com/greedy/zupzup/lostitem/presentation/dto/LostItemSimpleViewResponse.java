package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemSimpleViewCommand;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record LostItemSimpleViewResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String createdAt,
        String representativeImageUrl
) {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public static LostItemSimpleViewResponse from(LostItemSimpleViewCommand command) {
        String createdAt = command.createdAt()
                .atZone(KST)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return new LostItemSimpleViewResponse(
                command.id(),
                command.categoryId(),
                command.categoryName(),
                command.categoryIconUrl(),
                command.schoolAreaId(),
                command.schoolAreaName(),
                command.foundAreaDetail(),
                createdAt,
                command.representativeImageUrl()
        );
    }
}
