package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemImage;

public class LostItemImageFixture {

    public static LostItemImage DEFAULT_IMAGE(LostItem lostItem) {
        return LostItemImage.builder()
                .imageKey("https://example.com/default-image.jpg")
                .imageOrder(1)
                .lostItem(lostItem)
                .build();
    }
}
