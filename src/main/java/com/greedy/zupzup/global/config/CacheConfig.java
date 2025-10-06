package com.greedy.zupzup.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<CaffeineCache> cacheList = getCaffeineCaches();
        cacheManager.setCaches(cacheList);
        return cacheManager;
    }

    private List<CaffeineCache> getCaffeineCaches() {
        return Arrays.stream(CacheType.values())
                .map(cache -> new CaffeineCache(
                                cache.getCacheName(),
                                Caffeine.newBuilder()
                                        .recordStats()
                                        .expireAfterWrite(cache.getExpiredAfterWrite(), cache.getTimeUnit())
                                        .maximumSize(cache.getMaximumSize())
                                        .build()
                        )
                )
                .toList();
    }
}
