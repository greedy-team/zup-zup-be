package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.MyPledgedLostItemCommand;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record MyPledgedLostResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String categoryIconUrl,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String createdAt,
        String representativeImageUrl,
        String pledgedAt,
        String depositArea
) implements LostItemView {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static String toKstIso(LocalDateTime ts) {
        return ts.atZone(KST).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private static String pickListImage(MyPledgedLostItemCommand c) {
        final String repUrl = c.representativeImageUrl();
        if (hasText(repUrl)) {
            return repUrl;
        }
        return c.categoryIconUrl();
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public static MyPledgedLostResponse from(MyPledgedLostItemCommand c) {
        return new MyPledgedLostResponse(
                c.id(), c.categoryId(), c.categoryName(), c.categoryIconUrl(),
                c.schoolAreaId(), c.schoolAreaName(), c.foundAreaDetail(),
                toKstIso(c.createdAt()),
                pickListImage(c),toKstIso(c.pledgedAt()),c.depositArea()
        );
    }
}
