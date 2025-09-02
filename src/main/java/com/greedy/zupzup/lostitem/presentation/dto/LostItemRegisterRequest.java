package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.CreateLostItemCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

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
        @NotNull(message = "특징 옵션 목록은 null일 수 없습니다. 필요 없을 경우 빈 배열을 보내 주세요.")
        List<ItemFeatureRequest> featureOptions
) {
        public CreateLostItemCommand toCommand(List<MultipartFile> images) {
                return CreateLostItemCommand.of(this, images);
        }
}
