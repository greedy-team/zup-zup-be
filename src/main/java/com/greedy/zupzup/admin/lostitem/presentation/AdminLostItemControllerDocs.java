package com.greedy.zupzup.admin.lostitem.presentation;


import com.greedy.zupzup.admin.lostitem.presentation.dto.AdminPendingLostItemListResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsResponse;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsResponse;
import com.greedy.zupzup.auth.presentation.annotation.AdminAuth;
import com.greedy.zupzup.auth.presentation.argumentresolver.LoginAdmin;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;

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
            @AdminAuth LoginAdmin admin,
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
            @AdminAuth LoginAdmin admin,
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
            @AdminAuth LoginAdmin admin,
            @ParameterObject @Valid LostItemListRequest query
    );
}
