package com.greedy.zupzup.lostitem.application.dto;

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
}
