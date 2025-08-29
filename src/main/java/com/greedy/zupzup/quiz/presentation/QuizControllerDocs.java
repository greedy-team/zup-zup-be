package com.greedy.zupzup.quiz.presentation;

import com.greedy.zupzup.auth.presentation.argumentresolver.LoginMember;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.quiz.presentation.dto.QuizSubmissionRequest;
import com.greedy.zupzup.quiz.presentation.dto.QuizSubmissionResponse;
import com.greedy.zupzup.quiz.presentation.dto.QuizzesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Quiz", description = "분실물 주인 확인 퀴즈 관련 API")
public interface QuizControllerDocs {

    @Operation(
            summary = "분실물 퀴즈 조회",
            description = """
            특정 분실물의 주인을 판별하기 위한 퀴즈 목록을 조회합니다.
            **※ 로그인(액세스 토큰)이 반드시 필요한 API 입니다.**

            ### 요청 예시
            GET /api/lost-items/101/quizzes
                    
            ### 응답 예시
            ```json
            {
              "quizzes": [
                {
                  "featureId": 1,
                  "question": "어떤 브랜드의 제품인가요?",
                  "options": [
                    { "id": 1, "text": "삼성" },
                    { "id": 3, "text": "LG" },
                    { "id": 2, "text": "애플" },
                    { "id": 4, "text": "기타" }
                  ]
                },
                {
                  "featureId": 2,
                  "question": "제품의 색상은 무엇인가요?",
                  "options": [
                    { "id": 8, "text": "골드" },
                    { "id": 5, "text": "블랙" },
                    { "id": 7, "text": "실버" },
                    { "id": 9, "text": "기타" }
                  ]
                }
              ]
            }
            ```
            """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "퀴즈 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = QuizzesResponse.class),
                            examples = @ExampleObject(name = "퀴즈 조회 성공 예시", value = """
                                {
                                  "quizzes": [
                                    {
                                      "featureId": 1,
                                      "question": "어떤 브랜드의 제품인가요?",
                                      "options": [
                                        { "id": 1, "text": "삼성" },
                                        { "id": 3, "text": "LG" },
                                        { "id": 2, "text": "애플" },
                                        { "id": 4, "text": "기타" }
                                      ]
                                    }
                                  ]
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
                                  "instance": "/api/lost-items/101/quizzes"
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "이미 해당 퀴즈에 응시하여 '오답' 처리된 사용자가 재조회를 시도하는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "퀴즈 재시도 불가 예시", value = """
                                {
                                    "title": "퀴즈 시도 횟수 초과",
                                    "status": 403,
                                    "detail": "퀴즈 시도 횟수를 초과하여 더 이상 시도할 수 없습니다.",
                                    "instance": "/api/lost-items/101/quizzes"
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
                                  "instance": "/api/lost-items/101/quizzes"
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(responseCode = "409", description = "이미 주인이 확인되었거나 처리할 수 없는 상태의 분실물 퀴즈를 조회하려는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "서약 불가 상태 예시", value = """
                                {
                                  "title": "서약 불가 상태",
                                  "status": 409,
                                  "detail": "이미 서약되었거나 처리할 수 없는 상태의 분실물입니다.",
                                  "instance": "/api/lost-items/101/quizzes"
                                }
                                """
                            )
                    )
            )
    })
    ResponseEntity<QuizzesResponse> getLostItemQuizzes(
            @Parameter(description = "퀴즈를 조회할 분실물", required = true, example = "101")
            @PathVariable Long lostItemId,
            @Parameter(hidden = true) LoginMember loginMember
    );

    @Operation(
            summary = "퀴즈 정답 제출 및 채점",
            description = """
            사용자가 제출한 퀴즈 답안을 채점하고 정답 여부를 반환합니다.
            **※ 로그인(액세스 토큰)이 반드시 필요한 API 입니다.**

            ### 요청 본문 예시
            ```json
            {
              "answers": [
                { "featureId": 1, "selectedOptionId": 2 },
                { "featureId": 2, "selectedOptionId": 5 }
              ]
            }
            ```
            """,
            security = @SecurityRequirement(name = "zupzupAccessTokenAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채점 성공",
                    content = @Content(schema = @Schema(implementation = QuizSubmissionResponse.class),
                            examples = {
                                    @ExampleObject(name = "정답 예시", value = """
                                            {
                                              "correct": true,
                                              "detail": {
                                                "imageUrl": "https://zupzup-static-files.s3.ap-northeast-2.amazonaws.com/dev/iphone.webp",
                                                "description": "검정색 아이폰 15 프로입니다. 케이스는 투명색입니다."
                                              }
                                            }
                                            """),
                                    @ExampleObject(name = "오답 예시", value = """
                                            { "correct": false }
                                            """)
                            }
                    )
            ),
            @ApiResponse(responseCode = "401", description = "요청에 포함된 액세스 토큰이 없거나 유효하지 않아 인증에 실패한 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패 예시 (로그인 필요)", value = """
                                    {
                                      "title": "인증되지 않은 요청",
                                      "status": 401,
                                      "detail": "로그인이 필요합니다.",
                                      "instance": "/api/lost-items/101/quizzes"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "이미 해당 퀴즈에 응시하여 '오답' 처리된 사용자가 재조회를 시도하는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "퀴즈 재시도 불가 예시", value = """
                                    {
                                        "title": "퀴즈 시도 횟수 초과",
                                        "status": 403,
                                        "detail": "퀴즈 시도 횟수를 초과하여 더 이상 시도할 수 없습니다.",
                                        "instance": "/api/lost-items/101/quizzes"
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
                                      "instance": "/api/lost-items/101/quizzes"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "409", description = "이미 주인이 확인되었거나 처리할 수 없는 상태의 분실물 퀴즈를 조회하려는 경우",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "서약 불가 상태 예시", value = """
                                    {
                                      "title": "서약 불가 상태",
                                      "status": 409,
                                      "detail": "이미 서약되었거나 처리할 수 없는 상태의 분실물입니다.",
                                      "instance": "/api/lost-items/101/quizzes"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<QuizSubmissionResponse> submitQuizAnswers(
            @Parameter(description = "채점할 분실물", required = true, example = "101")
            @PathVariable Long lostItemId,
            @Parameter(hidden = true) LoginMember loginMember,
            @Valid @RequestBody QuizSubmissionRequest submissionRequest
    );
}
