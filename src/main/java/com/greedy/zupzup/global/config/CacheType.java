package com.greedy.zupzup.global.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public enum CacheType {

    ALL_SCHOOL_AREA(CacheNames.ALL_SCHOOL_AREA, 3, TimeUnit.DAYS, 1),
    ALL_CATEGORY(CacheNames.ALL_CATEGORY, 3, TimeUnit.DAYS, 1),
    CATEGORY_DETAILS(CacheNames.CATEGORY_DETAILS, 3, TimeUnit.DAYS, 20),
    ;

    public static final class CacheNames {
        public static final String ALL_SCHOOL_AREA = "all-school-area";
        public static final String ALL_CATEGORY = "all-category";
        public static final String CATEGORY_DETAILS = "category-details";
    }

    private final String cacheName;
    private final int expiredAfterWrite;
    private final TimeUnit timeUnit;
    private final int maximumSize;

}
