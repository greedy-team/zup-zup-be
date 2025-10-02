package com.greedy.zupzup.lostitem.presentation.dto;

public interface LostItemViewItem {
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
