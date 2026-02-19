package com.greedy.zupzup.admin.lostitem.application.dto;

import com.greedy.zupzup.lostitem.domain.LostItem;
import java.util.List;
import java.util.Map;

public record AdminLostItemResult(
        Long id,
        Long categoryId,
        String categoryName,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String createdAt,
        String description,
        String depositArea,
        List<String> imageUrl,
        List<AdminFeatureOptionDto> featureOptions
) {
    public static AdminLostItemResult from(
            LostItem item,
            List<String> imageUrls,
            List<AdminFeatureOptionDto> featureOptions
    ) {

        return new AdminLostItemResult(
                item.getId(),
                item.getCategory().getId(),
                item.getCategory().getName(),
                item.getFoundArea().getId(),
                item.getFoundArea().getAreaName(),
                item.getFoundAreaDetail(),
                item.getCreatedAt().toString(),
                item.getDescription(),
                item.getDepositArea(),
                imageUrls,
                featureOptions
        );
    }

    public static List<AdminLostItemResult> fromList(
            List<LostItem> items,
            Map<Long, List<String>> imageMap,
            Map<Long, List<AdminFeatureOptionDto>> featureMap
    ) {
        return items.stream()
                .map(item -> from(
                        item,
                        imageMap.getOrDefault(item.getId(), List.of()),
                        featureMap.getOrDefault(item.getId(), List.of())
                ))
                .toList();
    }
}
