package com.greedy.zupzup.alert.repository;

public interface AlertDigestProjection {
    String getEmail();
    String getCategoryName();
    String getCategoryEmoji();
    String getAreaName();
    Long getCount();
}
