package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import org.springframework.data.util.Pair;

import java.util.List;

public record LostItemRegisterData(
        Category category,
        SchoolArea foundSchoolArea,
        List<Pair<Feature, FeatureOption>> itemFeatureAndOptions
) {

    public boolean isNonETC() {
        return !itemFeatureAndOptions.isEmpty();
    }
}
