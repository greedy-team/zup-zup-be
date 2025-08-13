package com.greedy.zupzup.quiz.application.dto;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.exception.LostItemException;

public record LostItemDetailDto(
        String imageUrl,
        String description
) {

    public static LostItemDetailDto from(LostItem lostItem) {
        String imageUrl = lostItem.getImages().stream()
                .findFirst()
                .map(LostItemImage::getImageKey)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_IMAGE_NOT_FOUND));
        return new LostItemDetailDto(imageUrl, lostItem.getDescription());
    }
}
