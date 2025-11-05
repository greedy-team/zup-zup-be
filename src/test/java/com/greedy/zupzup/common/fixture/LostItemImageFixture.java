package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemImage;

public class LostItemImageFixture {

    public static LostItemImage DEFAULT_IMAGE(LostItem lostItem) {
        return LostItemImage.builder()
                .imageKey("https://example.com/default-image.jpg")
                .imageOrder(0)
                .lostItem(lostItem)
                .build();
    }

    public static LostItemImage SPECIFIC_IMAGE(LostItem lostItem, String imageKey, String imageUrl, boolean isRepresentative) {
        return LostItemImage.builder()
                .imageKey(imageKey)
                .imageOrder(isRepresentative ? 0 : 1)
                .lostItem(lostItem)
                .build();
    }
}
