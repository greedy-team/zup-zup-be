package com.greedy.zupzup.pledge.presentation;

import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.pledge.presentation.dto.PledgeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Pledge", description = "분실물 찾기 서약 관련 API")
public interface PledgeControllerDocs {

    @Operation(summary = "분실물 습득 서약 생성",
            description = """
                    사용자가 분실물을 찾기 전 악용 방지를 위해 서약을 생성합니다.
                    **※ 로그인(액세스 토큰)이 반드시 필요한 API 입니다.**
                    """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "서약 생성 성공",
                    content = @Content(schema = @Schema(implementation = PledgeResponse.class),
                            examples = @ExampleObject(name = "서약 생성 성공 예시", value = """
                                    {
                                        "pledgeId": 15,
                                        "lostItemId": 101,
                                        "ownerId": 2,
                                        "pledgeDateTime": "2025-08-29T14:30:00"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "퀴즈가 필요한 분실물에 대해 퀴즈를 통과하지 않은 사용자가 서약을 시도한 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "퀴즈 미통과(미시도) 예시", value = """
                                    {
                                      "title": "퀴즈 미시도",
                                      "status": 400,
                                      "detail": "퀴즈를 통과하지 않아 서약을 진행할 수 없습니다.",
                                      "instance": "/api/lost-items/101/pledges"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "요청에 포함된 액세스 토큰이 없거나 유효하지 않아 인증에 실패한 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패 예시 (로그인 필요)", value = """
                                    {
                                      "title": "인증되지 않은 요청",
                                      "status": 401,
                                      "detail": "로그인이 필요합니다.",
                                      "instance": "/api/lost-items/101/pledges"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 ID에 해당하는 분실물이 존재하지 않는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "분실물 없음 예시", value = """
                                    {
                                      "title": "분실물 없음",
                                      "status": 404,
                                      "detail": "해당 ID의 분실물을 찾을 수 없습니다.",
                                      "instance": "/api/lost-items/101/pledges"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "409", description = "이미 서약되었거나 처리할 수 없는 상태의 분실물에 대해 서약을 시도하는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "서약 불가 상태 예시", value = """
                                    {
                                      "title": "서약 불가 상태",
                                      "status": 409,
                                      "detail": "이미 서약되었거나 처리할 수 없는 상태의 분실물입니다.",
                                      "instance": "/api/lost-items/101/pledges"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<PledgeResponse> createPledge(
            @Parameter(description = "서약할 분실물", required = true, example = "12")
            @PathVariable Long lostItemId,
            @Parameter(hidden = true) LoginMember loginMember
    );
}
