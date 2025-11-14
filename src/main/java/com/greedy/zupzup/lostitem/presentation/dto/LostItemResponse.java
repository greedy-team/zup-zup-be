package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemListResult;
import com.greedy.zupzup.lostitem.application.dto.LostItemSimpleViewResult;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record LostItemResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String createdAt,
        String representativeImageUrl
) implements LostItemView {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static String toKstIso(LocalDateTime ts) {
        return ts.atZone(KST).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static LostItemResponse from(LostItemSimpleViewResult c) {
        return new LostItemResponse(
                c.id(), c.categoryId(), c.categoryName(), c.categoryIconUrl(),
                c.schoolAreaId(), c.schoolAreaName(), c.foundAreaDetail(),
                toKstIso(c.createdAt()),
                c.representativeImageUrl()
        );
    }

    public static LostItemResponse from(LostItemListResult c, String representativeImageUrl) {
        final String finalImage = pickListImage(c, representativeImageUrl);
        return new LostItemResponse(
                c.id(), c.categoryId(), c.categoryName(), c.categoryIconUrl(),
                c.schoolAreaId(), c.schoolAreaName(), c.foundAreaDetail(),
                toKstIso(c.createdAt()),
                finalImage
        );
    }

    private static String pickListImage(LostItemListResult c, String repUrl) {
        if ("기타".equals(c.categoryName())) {
            return repUrl;
        }
        return hasText(c.categoryIconUrl()) ? c.categoryIconUrl() : repUrl;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
