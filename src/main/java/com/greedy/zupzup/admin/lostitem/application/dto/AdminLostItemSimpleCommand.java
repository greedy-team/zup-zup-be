package com.greedy.zupzup.admin.lostitem.application.dto;

import java.util.List;

public record AdminLostItemSimpleCommand(
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
    public static AdminLostItemSimpleCommand from(AdminLostItemSimpleCommand c, List<AdminFeatureOptionDto> featureOptions) {

        return new AdminLostItemSimpleCommand(
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
