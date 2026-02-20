package com.greedy.zupzup.global.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTimeUtil {

    public static final String KST_ZONE_NAME = "Asia/Seoul";
    public static final ZoneId KST_ZONE_ID = ZoneId.of(KST_ZONE_NAME);

    private DateTimeUtil() {
    }

    public static OffsetDateTime toKstOffset(LocalDateTime localDateTime) {

        if (localDateTime == null) {
            return null;
        }

        return localDateTime
                .atZone(KST_ZONE_ID)
                .toOffsetDateTime();
    }

}
