package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record LostItemDetailViewResponse(
        Long id,
        String status,
        Category category,
        SchoolArea schoolArea,
        String locationDetail,
        String description,
        List<String> imageUrls,
        String depositArea,
        String pledgedAt,
        String createdAt,
        boolean quizRequired,
        boolean quizAnswered,
        boolean pledgedByMe
) {

    public static LostItemDetailViewResponse from(LostItemDetailViewCommand command) {

        String createdAt = command.createdAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String pledgedAt = command.pledgedAt() == null ? null
                : command.pledgedAt().toString();

        return new LostItemDetailViewResponse(
                command.id(),
                command.status().name(),
                new Category(command.categoryId(), command.categoryName(), command.categoryIconUrl()),
                new SchoolArea(command.schoolAreaId(), command.schoolAreaName()),
                command.locationDetail(),
                command.description(),
                command.imageUrls(),
                command.depositArea(),
                pledgedAt,
                createdAt,
                command.quizRequired(),
                command.quizAnswered(),
                command.pledgedByMe()
        );
    }

    public record Category(Long id, String name, String iconUrl) {
    }

    public record SchoolArea(Long id, String name) {
    }
}
