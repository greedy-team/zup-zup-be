package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemMultipartDocs;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterRequest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Lost Item Register", description = "분실물 등록 API")
public interface LostItemControllerDocs {

    @Operation(
            summary = "분실물 등록",
            description = "이미지를(최대 3장)과 분실물 정보(JSON)를 함께 업로드합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "분실물 등록에 성공힌 경우",
                    content = @Content(
                            schema = @Schema(implementation = LostItemRegisterResponse.class),
                            examples = @ExampleObject(
                                    name = "분실물 등록 성공 예시",
                                    value = """
                                            {
                                              "lostItemId": 1,
                                              "message": "분실물 등록에 성공했습니다."
                                            }
                                            """
                            )
                    )
            ),

            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "필수 입력값(보관 장소, 상세 습득장소)을 누락하여 요청한 경우",
                                            value = """
                                                    {
                                                      "title": "유효하지 않은 입력값",
                                                      "status": 400,
                                                      "detail": "depositArea: 분실물의 보관 장소를 자세히 입력해 주세요, foundAreaDetail: 분실물의 습득 장소의 자세한 정보를 입력해 주세요.",
                                                      "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "카테고리와 일치하지 않는 특징으로 등록 요청을 보낸 경우",
                                            value = """
                                                    {
                                                        "title": "잘못된 특징값",
                                                        "status": 400,
                                                        "detail": "요청하신 카테고리의 특징값이 해당 카테고리의 특징이 아닙니다.",
                                                        "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "카테고리와 특징값은 일치하지만 일치하지 않는 옵션으로 등록 요청을 보낸 경우",
                                            value = """
                                                    {
                                                        "title": "잘못된 옵션값",
                                                        "status": 400,
                                                        "detail": "요청하신 특징에 대한 옵션값이 해당 특징에 대한 옵션이 아닙니다.",
                                                        "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "이미지 파일 크기 초과",
                                            value = """
                                                    {
                                                       "title": "지원하지 않는 파일",
                                                       "status": 400,
                                                       "detail": "파일 크기는 10MB를 초과할 수 없습니다.",
                                                       "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "이미지 파일이 아닌 파일을 입력한 경우",
                                            value = """
                                                    {
                                                       "title": "지원하지 않는 파일",
                                                       "status": 400,
                                                       "detail": "이미지 파일만 업로드가 가능합니다.",
                                                       "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "사진이 입력되지 않은 경우",
                                            value = """
                                                    {
                                                       "title": "이미지가 입력되지 않음",
                                                       "status": 400,
                                                       "detail": "업로드할 이미지 파일을 선택해주세요.",
                                                       "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),

                                    @ExampleObject(
                                            name = "사진을 세장 이상 전송한 경우",
                                            value = """
                                                    {
                                                       "title": "잘못된 이미지 개수입니다.",
                                                       "status": 400,
                                                       "detail": "분실물 사진은 최소 1개 이상 3개 이하로 등록해야 합니다.",
                                                       "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),
                            }
                    )
            ),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리 or 학교 구역으로 등록을 요청한 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 카테고리 id로 요청",
                                            value = """
                                                    {
                                                      "title": "카테고리를 찾을 수 없음",
                                                      "status": 404,
                                                      "detail": "요청하신 카테고리가 존재하지 않습니다.",
                                                      "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 학교 구역 id로 요청",
                                            value = """
                                                    {
                                                      "title": "학교 구역을 찾을 수 없음",
                                                      "status": 404,
                                                      "detail": "요청하신 학교 구역이 존재하지 않습니다.",
                                                      "instance": "/api/lost-items"
                                                    }
                                                    """
                                    ),
                            }
                    )
            ),
            @ApiResponse(responseCode = "500", description = "이미지 저장에 실패한 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "이미지 업로드가 실패한 경우",
                                    value = """
                                            {
                                                "title": "이미지 업로드 실패",
                                                "status": 500,
                                                "detail": "이미지 업로드에 실패했습니다.",
                                                "instance": "/api/lost-items"
                                            }
                                            """

                            )
                    )
            )

    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = LostItemMultipartDocs.class),
                    encoding = {
                            @Encoding(name = "images", contentType = "image/*"),
                            @Encoding(name = "lostItemRegisterRequest", contentType = "application/json")
                    }
            )
    )
    ResponseEntity<LostItemRegisterResponse> create(
            @RequestPart("images") List<MultipartFile> images,
            @Valid @RequestPart("lostItemRegisterRequest") LostItemRegisterRequest lostItemRegisterRequest
    );
}
