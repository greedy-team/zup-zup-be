package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LostItemDetailViewCommand(
        Long id,
        LostItemStatus status,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String locationDetail,
        String description,
        List<String> imageUrls,
        String depositArea,
        LocalDate pledgedAt,
        LocalDateTime createdAt,
        boolean quizRequired,
        boolean quizAnswered,
        boolean pledgedByMe
) {
    public static LostItemDetailViewCommand of(LostItem item,
                                               List<String> imageUrls,
                                               String depositArea,
                                               boolean quizRequired,
                                               boolean quizAnswered,
                                               boolean pledgedByMe) {
        return new LostItemDetailViewCommand(
                item.getId(),
                item.getStatus(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getCategory().getIconUrl(),
                item.getFoundArea().getId(),
                item.getFoundArea().getAreaName(),
                item.getFoundAreaDetail(),
                item.getDescription(),
                imageUrls,
                depositArea,
                item.getPledgedAt(),
                item.getCreatedAt(),
                quizRequired,
                quizAnswered,
                pledgedByMe
        );
    }
}
