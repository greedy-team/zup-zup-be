package com.greedy.zupzup.lostitem.presentation.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = LostItemViewResponse.class)
public interface LostItemView {
    Long id();
    Long categoryId();
    String categoryName();
    String categoryIconUrl();
    Long schoolAreaId();
    String schoolAreaName();
    String foundAreaDetail();
    String createdAt();
    String representativeImageUrl();
}
