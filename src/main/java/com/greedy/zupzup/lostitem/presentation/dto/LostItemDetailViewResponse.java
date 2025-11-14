package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewResult;
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
        String createdAt,
        boolean quizRequired,
        boolean quizAnswered,
        boolean pledgedByMe
) {

    public static LostItemDetailViewResponse from(LostItemDetailViewResult command) {

        String createdAt = command.createdAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        return new LostItemDetailViewResponse(
                command.id(),
                command.status().name(),
                new Category(command.categoryId(), command.categoryName(), command.categoryIconUrl()),
                new SchoolArea(command.schoolAreaId(), command.schoolAreaName()),
                command.locationDetail(),
                command.description(),
                command.imageUrls(),
                command.depositArea(),
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
