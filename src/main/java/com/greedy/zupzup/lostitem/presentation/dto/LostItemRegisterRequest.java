package com.greedy.zupzup.lostitem.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LostItemRegisterRequest(

        String description,

        @NotBlank(message = "분실물의 보관 장소를 자세히 입력해 주세요")
        String depositArea,

        @NotNull(message = "분실물의 습득 장소(구역)를 선택해 주세요.")
        Long foundAreaId,

        @NotBlank(message = "분실물의 습득 장소의 자세한 정보를 입력해 주세요.")
        String foundAreaDetail,

        @NotNull(message = "분실물의 카테고리를 선택해 주세요.")
        Long categoryId,

        @Valid
        @NotEmpty(message = "분실물의 특징을 하나 이상 선택해 주세요.")
        List<ItemFeatureRequest> featureOptions,

        @Size(max = 3, message = "분실물의 이미지는 최대 3개까지 등록할 수 있습니다.")
        @NotEmpty(message = "분실물의 사진을 하나 이상 등록해 주세요.")
        List<String> imageOrder     // 파일 이름 리스트
) {
}
