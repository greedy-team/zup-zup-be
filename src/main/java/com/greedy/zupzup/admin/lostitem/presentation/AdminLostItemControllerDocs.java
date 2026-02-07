package com.greedy.zupzup.admin.lostitem.presentation;


import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.UpdateLostItemRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.UpdateLostItemResponse;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin - LostItem", description = "관리자 분실물 승인/거절/조회 API")
public interface AdminLostItemControllerDocs {

    @Operation(
            summary = "보류 분실물 승인(상태를 REGISTERED로 변경)",
            description = """
                    관리자 권한으로 보류(PENDING) 상태의 분실물을 승인 처리합니다.
                    - 상태가 REGISTERED로 변경됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "승인 성공",
                    content = @Content(schema = @Schema(implementation = ApproveLostItemsResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                "successfulCount": 1,
                                                "totalRequestedCount": 1,
                                                "message": "1건의 분실물이 승인되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<ApproveLostItemsResponse> approveBulk(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "승인할 분실물 ID 목록",
                    content = @Content(schema = @Schema(implementation = ApproveLostItemsRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "lostItemIds": [1, 2, 3]
                                            }
                                            """
                            )
                    )
            )
            ApproveLostItemsRequest request
    );

    @Operation(
            summary = "보류 분실물 삭제(거절)",
            description = """
                    관리자 권한으로 보류 상태의 분실물을 삭제합니다.
                    - DB에서 완전히 삭제됩니다.
                    - 관련 이미지도 삭제됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = RejectLostItemsResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                 "successfulCount": 1,
                                                 "totalRequestedCount": 1,
                                                 "message": "1건의 분실물이 삭제되었습니다."
                                             }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<RejectLostItemsResponse> rejectBulk(
            @Valid @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "삭제할 분실물 ID 목록",
                    content = @Content(schema = @Schema(implementation = RejectLostItemsRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "lostItemIds": [7, 8]
                                            }
                                            """
                            )
                    )
            )
            RejectLostItemsRequest request
    );

    @Operation(
            summary = "보류 중 분실물 목록 조회",
            description = """
                    승인 대기(PENDING) 중인 분실물 목록을 조회합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AdminPendingLostItemListResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                                 "count": 1,
                                                 "items": [
                                                     {
                                                         "id": 1,
                                                         "categoryId": 1,
                                                         "categoryName": "전자기기",
                                                         "schoolAreaId": 1,
                                                         "schoolAreaName": "대양 AI 센터",
                                                         "foundAreaDetail": "상세위치",
                                                         "createdAt": "2025-11-07T10:41:44.025705",
                                                         "description": "test item",
                                                         "depositArea": "보관장소",
                                                         "imageUrl": [
                                                             "imgA",
                                                             "imgB",
                                                             "imgC"
                                                         ],
                                                         "featureOptions": [
                                                             {
                                                                 "id": 1,
                                                                 "optionValue": "삼성",
                                                                 "quizQuestion": "어떤 브랜드의 제품인가요?"
                                                             },
                                                             {
                                                                 "id": 5,
                                                                 "optionValue": "블랙",
                                                                 "quizQuestion": "제품의 색상은 무엇인가요?"
                                                             }
                                                         ]
                                                     }
                                                 ],
                                                 "pageInfo": {
                                                     "page": 1,
                                                     "size": 10,
                                                     "totalElements": 1,
                                                     "totalPages": 1,
                                                     "hasPrev": false,
                                                     "hasNext": false
                                                 }
                                             }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ADMIN 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<AdminPendingLostItemListResponse> listPending(
            @ParameterObject @Valid LostItemListRequest query
    );

    @Operation(
            summary = "보류 분실물 상세 정보 수정 및 승인",
            description = """
                    관리자 권한으로 보류(PENDING) 상태인 분실물의 정보를 수정하고 즉시 승인(REGISTERED) 처리합니다.
                    - 사진 수정: 유지할 이미지 ID 목록(`keepImageIds`)과 새 이미지 파일(`newImages`)을 함께 보냅니다.
                    - 사진을 변경하지 않을 경우: 기존 이미지 ID들을 `keepImageIds`에 모두 담고, `newImages`는 비워서 보냅니다.
                    - 정보 수정: 카테고리, 특징 옵션, 설명 등을 `updateRequest`에 담아 보냅니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 및 승인 성공",
                    content = @Content(schema = @Schema(implementation = UpdateLostItemResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미지 개수 부족, 필수 특징값 누락 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 또는 이미 승인된 물건 수정 시도",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 분실물 ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })

    ResponseEntity<UpdateLostItemResponse> updateLostItem(
            @Schema(description = "수정할 분실물 ID", example = "1")
            Long lostItemId,

            @Schema(description = "새로 업로드할 이미지 파일 리스트")
            List<MultipartFile> newImages,

            @Schema(description = "유지할 기존 이미지 ID 리스트", example = "[1, 2]")
            List<Long> keepImageIds,

            @Valid
            @RequestBody(
                    description = "수정할 텍스트 정보 및 특징 옵션",
                    content = @Content(
                            mediaType = org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateLostItemRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "description": "수정된 상세 설명입니다.",
                                              "depositArea": "학생회관 2층",
                                              "foundAreaId": 1,
                                              "foundAreaDetail": "대양 AI센터 B2층",
                                              "categoryId": 1,
                                              "featureOptions": [
                                                { "featureId": 1, "optionId": 1 },
                                                { "featureId": 2, "optionId": 5 }
                                              ]
                                            }
                                            """
                            )
                    )
            )
            UpdateLostItemRequest updateRequest
    );
}
