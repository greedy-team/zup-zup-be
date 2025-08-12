package com.greedy.zupzup.lostitem.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record ItemFeatureRequest(

        @NotNull(message = "분실물의 특징을 선택해 주세요.")
        Long featureId,

        @NotNull(message = "분실물의 특징에 대한 옵션을 선택해 주세요.")
        Long optionId
) {
}
