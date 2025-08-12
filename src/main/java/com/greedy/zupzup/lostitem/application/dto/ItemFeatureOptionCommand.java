package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.presentation.dto.ItemFeatureRequest;

public record ItemFeatureOption(
        Long featureId,
        Long optionId
) {
    public static ItemFeatureOption from(ItemFeatureRequest request) {
        return new ItemFeatureOption(
                request.featureId(),
                request.optionId()
        );
    }
}
