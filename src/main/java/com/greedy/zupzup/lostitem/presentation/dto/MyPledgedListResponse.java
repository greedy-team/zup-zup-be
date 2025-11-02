package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.MyPledgedLostItemCommand;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record MyPledgedListResponse(
        Long id,
        Long categoryId,
        String categoryName,
        Long schoolAreaId,
        String schoolAreaName,
        String foundAreaDetail,
        String createdAt,
        String representativeImageUrl,
        String pledgedAt,
        String depositArea
) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static String toKstIso(LocalDateTime ts) {
        return ts.atZone(KST).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private static String pickListImage(MyPledgedLostItemCommand c) {
        return c.representativeImageUrl();
    }

    public static MyPledgedListResponse from(MyPledgedLostItemCommand c) {
        return new MyPledgedListResponse(
                c.id(),
                c.categoryId(),
                c.categoryName(),
                c.schoolAreaId(),
                c.schoolAreaName(),
                c.foundAreaDetail(),
                toKstIso(c.createdAt()),
                pickListImage(c),
                toKstIso(c.pledgedAt()),
                c.depositArea()
        );
    }
}
