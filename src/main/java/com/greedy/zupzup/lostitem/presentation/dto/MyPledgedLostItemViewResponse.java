package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.MyPledgedLostItemCommand;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record MyPledgedLostItemViewResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String createdAt,
        String representativeImageUrl
) implements LostItemViewItem {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static String toKstIso(LocalDateTime ts) {
        return ts.atZone(KST).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private static String pickListImage(MyPledgedLostItemCommand c) {
        final String repUrl = c.representativeImageUrl();
        if ("기타".equals(c.categoryName())) {
            return repUrl;
        }
        return hasText(c.categoryIconUrl()) ? c.categoryIconUrl() : repUrl;
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public static MyPledgedLostItemViewResponse from(MyPledgedLostItemCommand c) {
        return new MyPledgedLostItemViewResponse(
                c.id(), c.categoryId(), c.categoryName(), c.categoryIconUrl(),
                c.schoolAreaId(), c.schoolAreaName(), c.foundAreaDetail(),
                toKstIso(c.createdAt()),
                pickListImage(c)
        );
    }
}
