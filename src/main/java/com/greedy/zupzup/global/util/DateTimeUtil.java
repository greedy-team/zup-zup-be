package com.greedy.zupzup.global.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTimeUtil {

    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

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
