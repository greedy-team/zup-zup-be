package com.greedy.zupzup.lostitem.repository;

import java.time.LocalDateTime;

public interface FoundItemProjection {
    Long getId();
    Long getCategoryId();
    String getCategoryName();
    Long getSchoolAreaId();
    String getSchoolAreaName();
    String getFoundAreaDetail();
    String getRepresentativeImageUrl();
    String getDescription();
    LocalDateTime getCreatedAt();
    LocalDateTime getPledgedAt();
}
