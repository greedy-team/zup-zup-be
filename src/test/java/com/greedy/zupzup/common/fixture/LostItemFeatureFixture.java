package com.greedy.zupzup.common.fixture;

import static com.greedy.zupzup.common.fixture.FeatureFixture.ELECTRONIC_BRAND;
import static com.greedy.zupzup.common.fixture.FeatureFixture.ELECTRONIC_COLOR;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.ELECTRONIC_BRAND_OPTIONS;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.ELECTRONIC_COLOR_OPTIONS;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class LostItemFeatureFixture {

    public static List<LostItemFeature> ELECTRONIC_LOST_ITEM_FEATURES(LostItem lostItem) {
        Category category = lostItem.getCategory();
        AtomicLong featureIdCounter = new AtomicLong(1L);
        AtomicLong optionIdCounter = new AtomicLong(1L);

        // 1. 브랜드 특징 및 정답 설정
        Feature brandFeature = ELECTRONIC_BRAND(category);
        ReflectionTestUtils.setField(brandFeature, "id", featureIdCounter.getAndIncrement());

        List<FeatureOption> brandOptions = ELECTRONIC_BRAND_OPTIONS(brandFeature);
        brandOptions.forEach(option ->
                ReflectionTestUtils.setField(option, "id", optionIdCounter.getAndIncrement())
        );
        FeatureOption selectedBrandOption = brandOptions.get(0); // 삼성이 정답
        ReflectionTestUtils.setField(brandFeature, "options", brandOptions);
        LostItemFeature brandLostItemFeature = new LostItemFeature(lostItem, brandFeature, selectedBrandOption);

        // 2. 색상 특징 및 정답 설정
        Feature colorFeature = ELECTRONIC_COLOR(category);
        ReflectionTestUtils.setField(colorFeature, "id", featureIdCounter.getAndIncrement());

        List<FeatureOption> colorOptions = ELECTRONIC_COLOR_OPTIONS(colorFeature);
        colorOptions.forEach(option ->
                ReflectionTestUtils.setField(option, "id", optionIdCounter.getAndIncrement())
        );
        FeatureOption selectedColorOption = colorOptions.get(0); // 블랙이 정답
        ReflectionTestUtils.setField(colorFeature, "options", colorOptions);
        LostItemFeature colorLostItemFeature = new LostItemFeature(lostItem, colorFeature, selectedColorOption);

        return List.of(brandLostItemFeature, colorLostItemFeature);
    }
}
