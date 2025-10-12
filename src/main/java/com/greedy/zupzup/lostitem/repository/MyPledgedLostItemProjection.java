package com.greedy.zupzup.lostitem.repository;

import java.time.LocalDateTime;

public interface MyPledgedLostItemProjection {
    Long getId();
    Long getCategoryId();
    String getCategoryName();
    String getCategoryIconUrl();
    Long getSchoolAreaId();
    String getSchoolAreaName();
    String getFoundAreaDetail();
    LocalDateTime getCreatedAt();
    String getRepresentativeImageUrl();
    LocalDateTime getPledgedAt();
    String getDepositArea();
}
