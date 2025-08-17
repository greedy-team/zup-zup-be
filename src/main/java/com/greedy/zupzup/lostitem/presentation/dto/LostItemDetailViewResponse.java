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
        List<String> imageKeys,
        String depositArea,
        String pledgedAt,
        String createdAt,
        boolean quizRequired,
        boolean quizAnswered,
        boolean pledgedByMe
) {

    public static LostItemDetailViewResponse from(LostItemDetailViewCommand c) {

        String createdAt = c.createdAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        String pledgedAt = c.pledgedAt() == null ? null
                : c.pledgedAt().toString();

        return new LostItemDetailViewResponse(
                c.id(),
                c.status().name(),
                new Category(c.categoryId(), c.categoryName(), c.categoryIconUrl()),
                new SchoolArea(c.schoolAreaId(), c.schoolAreaName()),
                c.locationDetail(),
                c.description(),
                c.imageUrls(),
                c.depositArea(),
                pledgedAt,
                createdAt,
                c.quizRequired(),
                c.quizAnswered(),
                c.pledgedByMe()
        );
    }

    public record Category(Long id, String name, String iconUrl) {
    }

    public record SchoolArea(Long id, String name) {
    }
}
