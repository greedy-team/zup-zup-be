package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.category.domain.Category;

public class CategoryFixture {

    public static Category ELECTRONIC() {
        return Category
                .builder()
                .name("전자기기")
                .iconUrl("https://cdn-icons-png.flaticon.com/128/519/519184.png")
                .build();
    }

    public static Category WALLET() {
        return Category
                .builder()
                .name("지갑")
                .iconUrl("https://cdn-icons-png.flaticon.com/128/519/519184.png")
                .build();
    }
}
