package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.presentation.dto.CancelPledgeResponse;
import com.greedy.zupzup.lostitem.presentation.dto.FoundCompleteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

@Tag(name = "Lost Item Pledge", description = "분실물 서약 및 상태 변경 API")
public interface LostItemPledgeControllerDocs {

    @Operation(
            summary = "서약 취소",
            description = "서약 상태인 분실물을 다시 등록 상태(REGISTERED)로 되돌립니다. 기존 서약 데이터는 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "서약 취소 성공",
                    content = @Content(
                            schema = @Schema(implementation = CancelPledgeResponse.class),
                            examples = @ExampleObject(
                                    name = "서약 취소 성공 예시",
                                    value = """
                                            {
                                              "lostItemId": 1,
                                              "status": "REGISTERED",
                                              "message": "서약이 정상적으로 취소되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인이 서약한 물건이 아님)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "다른 사용자의 서약을 취소하려는 경우",
                                    value = """
                                            {
                                              "title": "권한 없음",
                                              "status": 403,
                                              "detail": "본인이 서약한 분실물만 취소할 수 있습니다.",
                                              "instance": "/api/lost-items/1/pledge/cancel"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "분실물 또는 서약 데이터 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 분실물 ID",
                                            value = """
                                                    {
                                                      "title": "분실물을 찾을 수 없음",
                                                      "status": 404,
                                                      "detail": "해당 ID의 분실물이 존재하지 않습니다.",
                                                      "instance": "/api/lost-items/999/pledge/cancel"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "서약 데이터가 없는 경우",
                                            value = """
                                                    {
                                                      "title": "서약 정보 없음",
                                                      "status": 404,
                                                      "detail": "해당 분실물에 대한 서약 정보를 찾을 수 없습니다.",
                                                      "instance": "/api/lost-items/1/pledge/cancel"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "409", description = "잘못된 상태에서의 요청",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "서약 상태(PLEDGED)가 아닌 분실물을 취소하려는 경우",
                                    value = """
                                            {
                                              "title": "잘못된 분실물 상태",
                                              "status": 409,
                                              "detail": "서약 상태인 분실물만 취소할 수 있습니다.",
                                              "instance": "/api/lost-items/1/pledge/cancel"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{id}/pledge/cancel")
    ResponseEntity<CancelPledgeResponse> cancelPledge(
            @Parameter(description = "분실물 ID", required = true, example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("loginMemberId") Long memberId
    );


    @Operation(
            summary = "찾음(습득) 처리 완료",
            description = "서약자가 분실물 상태를 찾음(FOUND)으로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "찾음 처리 성공",
                    content = @Content(
                            schema = @Schema(implementation = FoundCompleteResponse.class),
                            examples = @ExampleObject(
                                    name = "찾음 처리 성공 예시",
                                    value = """
                                            {
                                              "lostItemId": 1,
                                              "status": "FOUND",
                                              "message": "습득 완료되었습니다."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "서약자가 아닌 사용자가 찾음 처리를 시도한 경우",
                                    value = """
                                            {
                                              "title": "권한 없음",
                                              "status": 403,
                                              "detail": "서약한 사용자만 찾음 처리를 할 수 있습니다.",
                                              "instance": "/api/lost-items/1/found"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "409", description = "잘못된 상태",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "서약 상태가 아닌데 찾음 처리를 시도한 경우",
                                    value = """
                                            {
                                              "title": "잘못된 분실물 상태",
                                              "status": 409,
                                              "detail": "서약 상태인 분실물만 찾음 처리가 가능합니다.",
                                              "instance": "/api/lost-items/1/found"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/{id}/found")
    ResponseEntity<FoundCompleteResponse> completeFound(
            @Parameter(description = "분실물 ID", required = true, example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute("loginMemberId") Long memberId
    );
}
