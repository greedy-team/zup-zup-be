package com.greedy.zupzup.admin.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.presentation.dto.ItemFeatureRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UpdateLostItemRequest(
        @NotBlank(message = "설명은 비어 있을 수 없습니다.")
        String description,

        @NotBlank(message = "보관 장소를 입력해 주세요.")
        String depositArea,

        @NotNull(message = "습득 구역 ID는 필수입니다.")
        Long foundAreaId,

        @NotBlank(message = "상세 습득 장소를 입력해 주세요.")
        String foundAreaDetail,

        @NotNull(message = "카테고리 ID는 필수입니다.")
        Long categoryId,

        @Valid
        @NotNull(message = "특징 옵션 목록은 필수입니다.")
        List<ItemFeatureRequest> featureOptions
) {}
