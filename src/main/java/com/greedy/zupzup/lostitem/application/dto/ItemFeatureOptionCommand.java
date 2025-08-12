package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.presentation.dto.ItemFeatureRequest;

public record ItemFeatureOptionCommand(
        Long featureId,
        Long optionId
) {
    public static ItemFeatureOptionCommand from(ItemFeatureRequest request) {
        return new ItemFeatureOptionCommand(
                request.featureId(),
                request.optionId()
        );
    }
}
