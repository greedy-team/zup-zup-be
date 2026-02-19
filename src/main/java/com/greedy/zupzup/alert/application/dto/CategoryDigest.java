package com.greedy.zupzup.alert.application.dto;

import java.util.Map;

public record CategoryDigest(
        String categoryName,
        String emoji,
        long totalCount,
        Map<String, Long> itemsByLocation
) {

    public static CategoryDigest of(
            String categoryName,
            String emoji,
            long totalCount,
            Map<String, Long> itemsByLocation
    ) {
        return new CategoryDigest(
                categoryName,
                emoji,
                totalCount,
                itemsByLocation
        );
    }
}
