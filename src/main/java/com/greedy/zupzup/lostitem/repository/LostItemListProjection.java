package com.greedy.zupzup.lostitem.repository;

import java.time.LocalDateTime;

public interface LostItemListProjection {
    Long getId();

    Long getCategoryId();

    String getCategoryName();

    String getCategoryIconUrl();

    Long getSchoolAreaId();

    String getSchoolAreaName();

    String getFindArea();

    LocalDateTime getCreatedAt();
}
