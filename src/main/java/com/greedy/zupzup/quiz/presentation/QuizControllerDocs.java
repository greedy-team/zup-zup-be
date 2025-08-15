package com.greedy.zupzup.quiz.presentation;

import com.greedy.zupzup.quiz.presentation.dto.QuizSubmissionRequest;
import com.greedy.zupzup.quiz.presentation.dto.QuizSubmissionResponse;
import com.greedy.zupzup.quiz.presentation.dto.QuizzesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Quiz", description = "분실물 주인 확인 퀴즈 관련 API")
public interface QuizControllerDocs {

    @Operation(
            summary = "분실물 퀴즈 조회",
            description = """
            특정 분실물의 주인을 판별하기 위한 퀴즈 목록을 조회합니다.

            ### 요청 예시
            GET /quizzes/{lostItemId}?memberId=1

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
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "퀴즈 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "요청한 분실물을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    ResponseEntity<QuizzesResponse> getLostItemQuizzes(
            @Parameter(description = "퀴즈를 조회할 분실물", required = true, example = "101")
            @PathVariable Long lostItemId,
            @Parameter(description = "퀴즈를 푸는 사용자", required = true, example = "7")
            @RequestParam Long memberId
    );

    @Operation(
            summary = "퀴즈 정답 제출 및 채점",
            description = """
            사용자가 제출한 퀴즈 답안을 채점하고 정답 여부를 반환합니다.

            ### 요청 본문 예시
            ```json
            {
              "answers": [
                { "featureId": 1, "selectedOptionId": 2 },
                { "featureId": 2, "selectedOptionId": 5 }
              ]
            }
            ```

            ### 응답 예시
            - 정답:
            ```json
            {
              "correct": true,
              "detail": {
                "imageUrl": "https://zupzup-static-files.s3.ap-northeast-2.amazonaws.com/dev/iphone.webp",
                "description": "검정색 아이폰 15 프로입니다. 케이스는 투명색입니다."
              }
            }
            ```
            - 오답:
            ```json
            { "correct": false }
            ```
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "채점 성공"),
            @ApiResponse(responseCode = "400", description = "요청 형식 오류 (예: answers 배열 비어있음)"),
            @ApiResponse(responseCode = "404", description = "요청한 분실물을 찾을 수 없음")
    })
    ResponseEntity<QuizSubmissionResponse> submitQuizAnswers(
            @Parameter(description = "채점할 분실물", required = true, example = "101")
            @PathVariable Long lostItemId,
            @Parameter(description = "퀴즈를 푸는 사용자", required = true, example = "7")
            @RequestParam Long memberId,
            @Valid @RequestBody QuizSubmissionRequest submissionRequest
    );
}
