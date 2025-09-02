package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.application.dto.LostItemSimpleViewCommand;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record LostItemViewResponse(
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

    private static String toKstIso(LocalDateTime ts) {
        return ts.atZone(KST).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static LostItemViewResponse from(LostItemSimpleViewCommand c) {
        return new LostItemViewResponse(
                c.id(), c.categoryId(), c.categoryName(), c.categoryIconUrl(),
                c.schoolAreaId(), c.schoolAreaName(), c.foundAreaDetail(),
                toKstIso(c.createdAt()),
                c.representativeImageUrl()
        );
    }

    public static LostItemViewResponse from(LostItemListCommand c, String representativeImageUrl) {
        final String finalImage = pickListImage(c, representativeImageUrl);
        return new LostItemViewResponse(
                c.id(), c.categoryId(), c.categoryName(), c.categoryIconUrl(),
                c.schoolAreaId(), c.schoolAreaName(), c.foundAreaDetail(),
                toKstIso(c.createdAt()),
                finalImage
        );
    }

    private static String pickListImage(LostItemListCommand c, String repUrl) {
        if ("기타".equals(c.categoryName())) {
            return repUrl;
        }
        return hasText(c.categoryIconUrl()) ? c.categoryIconUrl() : repUrl;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
