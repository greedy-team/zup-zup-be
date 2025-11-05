package com.greedy.zupzup.admin.lostitem.repository;

import java.time.LocalDateTime;

public interface AdminLostItemSimpleProjection {
    Long getId();
    Long getCategoryId();
    String getCategoryName();
    Long getSchoolAreaId();
    String getSchoolAreaName();
    String getFoundAreaDetail();
    String getDescription();
    String getDepositArea();
    LocalDateTime getCreatedAt();
    String getImageKeysString();
}
