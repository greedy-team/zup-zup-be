package com.greedy.zupzup.admin.lostitem.application.dto;

import java.util.List;

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
    public static AdminLostItemResult from(AdminLostItemResult c, List<AdminFeatureOptionDto> featureOptions) {

        return new AdminLostItemResult(
                c.id(),
                c.categoryId(),
                c.categoryName(),
                c.schoolAreaId(),
                c.schoolAreaName(),
                c.foundAreaDetail,
                c.createdAt(),
                c.description(),
                c.depositArea(),
                c.imageUrl,
                featureOptions
        );
    }
}
