package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;

import java.util.List;

public class FeatureFixture {

    // ================= 잔자기기 특징 =================
    public static List<Feature> ELECTRONIC_FEATURES(Category electronics) {
        return List.of(
                ELECTRONIC_BRAND(electronics),
                ELECTRONIC_COLOR(electronics)
        );
    }

    public static Feature ELECTRONIC_BRAND(Category electronics) {
        return Feature.builder().name("브랜드").quizQuestion("어떤 브랜드의 제품인가요?").category(electronics).build();
    }

    public static Feature ELECTRONIC_COLOR(Category electronics) {
        return Feature.builder().name("색상").quizQuestion("제품의 색상은 무엇인가요?").category(electronics).build();
    }


    // ================= 지갑 특징 =================
    public static List<Feature> WALLET_FEATURES(Category wallet) {
        return List.of(
                WALLET_TYPE(wallet),
                WALLET_COLOR(wallet)
        );
    }

    public static Feature WALLET_TYPE(Category wallet) {
        return Feature.builder().name("종류").quizQuestion("어떤 종류의 지갑/카드인가요?").category(wallet).build();
    }

    public static Feature WALLET_COLOR(Category wallet) {
        return Feature.builder().name("종류").quizQuestion("어떤 종류의 지갑인가요?").category(wallet).build();
    }
}

