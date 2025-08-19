package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;

import java.util.List;

public class FeatureOptionFixture {

    // ================= 전자기기 브랜드 옵션 =================
    public static List<FeatureOption> ELECTRONIC_BRAND_OPTIONS(Feature brand) {
        return List.of(
                ELECTRONIC_BRAND_SAMSUNG(brand),
                ELECTRONIC_BRAND_APPLE(brand),
                ELECTRONIC_BRAND_LG(brand),
                ELECTRONIC_BRAND_ECT(brand)
        );
    }

    public static FeatureOption ELECTRONIC_BRAND_SAMSUNG(Feature brand) {
        return FeatureOption.builder().optionValue("삼성").feature(brand).build();
    }
    public static FeatureOption ELECTRONIC_BRAND_APPLE(Feature brand) {
        return FeatureOption.builder().optionValue("애플").feature(brand).build();
    }
    public static FeatureOption ELECTRONIC_BRAND_LG(Feature brand) {
        return FeatureOption.builder().optionValue("LG").feature(brand).build();
    }
    public static FeatureOption ELECTRONIC_BRAND_ECT(Feature brand) {
        return FeatureOption.builder().optionValue("기타").feature(brand).build();
    }

    // ================= 전자기기 색상 옵션 =================
    public static List<FeatureOption> ELECTRONIC_COLOR_OPTIONS(Feature color) {
        return List.of(
                ELECTRONIC_COLOR_BLACK(color),
                ELECTRONIC_COLOR_WHITE(color),
                ELECTRONIC_COLOR_SILVER(color),
                ELECTRONIC_COLOR_GOLD(color),
                ELECTRONIC_COLOR_ECT(color)
        );
    }

    public static FeatureOption ELECTRONIC_COLOR_BLACK(Feature color) {
        return FeatureOption.builder().optionValue("블랙").feature(color).build();
    }
    public static FeatureOption ELECTRONIC_COLOR_WHITE(Feature color) {
        return FeatureOption.builder().optionValue("화이트").feature(color).build();
    }
    public static FeatureOption ELECTRONIC_COLOR_SILVER(Feature color) {
        return FeatureOption.builder().optionValue("실버").feature(color).build();
    }
    public static FeatureOption ELECTRONIC_COLOR_GOLD(Feature color) {
        return FeatureOption.builder().optionValue("골드").feature(color).build();
    }
    public static FeatureOption ELECTRONIC_COLOR_ECT(Feature color) {
        return FeatureOption.builder().optionValue("기타").feature(color).build();
    }


    // ================= 지갑 종류 옵션 =================
    public static List<FeatureOption> WALLET_TYPE_OPTIONS(Feature type) {
        return List.of(
                WALLET_TYPE_HALF(type),
                WALLET_TYPE_LONG(type),
                WALLET_TYPE_CARD(type),
                WALLET_TYPE_COIN(type),
                WALLET_TYPE_ECT(type)
        );
    }

    public static FeatureOption WALLET_TYPE_HALF(Feature type) {
        return FeatureOption.builder().optionValue("반지갑").feature(type).build();
    }
    public static FeatureOption WALLET_TYPE_LONG(Feature type) {
        return FeatureOption.builder().optionValue("장지갑").feature(type).build();
    }
    public static FeatureOption WALLET_TYPE_CARD(Feature type) {
        return FeatureOption.builder().optionValue("카드지갑").feature(type).build();
    }
    public static FeatureOption WALLET_TYPE_COIN(Feature type) {
        return FeatureOption.builder().optionValue("동전지갑").feature(type).build();
    }
    public static FeatureOption WALLET_TYPE_ECT(Feature type) {
        return FeatureOption.builder().optionValue("기타").feature(type).build();
    }

    // ================= 지갑 색상 옵션 =================
    public static List<FeatureOption> WALLET_COLOR_OPTIONS(Feature color) {
        return List.of(
                WALLET_COLOR_BLACK(color),
                WALLET_COLOR_BROWN(color),
                WALLET_COLOR_BLUE(color),
                WALLET_COLOR_WHITE(color),
                WALLET_COLOR_ECT(color)
        );
    }

    public static FeatureOption WALLET_COLOR_BLACK(Feature color) {
        return FeatureOption.builder().optionValue("블랙").feature(color).build();
    }
    public static FeatureOption WALLET_COLOR_BROWN(Feature color) {
        return FeatureOption.builder().optionValue("브라운").feature(color).build();
    }
    public static FeatureOption WALLET_COLOR_BLUE(Feature color) {
        return FeatureOption.builder().optionValue("블루/네이비").feature(color).build();
    }
    public static FeatureOption WALLET_COLOR_WHITE(Feature color) {
        return FeatureOption.builder().optionValue("화이트").feature(color).build();
    }
    public static FeatureOption WALLET_COLOR_ECT(Feature color) {
        return FeatureOption.builder().optionValue("기타").feature(color).build();
    }

}
