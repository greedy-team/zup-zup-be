package com.greedy.zupzup.global.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public enum CacheType {

    ALL_SCHOOL_AREA(CacheNames.ALL_SCHOOL_AREA, 3, TimeUnit.DAYS, 1),
    ;


    public static final class CacheNames {
        public static final String ALL_SCHOOL_AREA = "school-area-all";
    }

    private final String cacheName;
    private final int expiredAfterWrite;
    private final TimeUnit timeUnit;
    private final int maximumSize;

}
