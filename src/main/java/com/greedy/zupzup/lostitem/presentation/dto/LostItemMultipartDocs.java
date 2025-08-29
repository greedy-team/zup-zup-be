package com.greedy.zupzup.lostitem.presentation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(name = "LostItemMultipartDoc", description = "분실물 등록 멀티파트 폼")
public record LostItemMultipartDocs(
        @ArraySchema(
                arraySchema = @Schema(description = "분실물 이미지 파일 리스트"),
                schema = @Schema(type = "string", format = "binary")
        )
        List<MultipartFile> images,

        @Schema(
                description = "분실물 등록 요청 JSON",
                implementation = LostItemRegisterRequest.class
        )
        LostItemRegisterRequest lostItemRegisterRequest
) {
}
