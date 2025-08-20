package com.greedy.zupzup.quiz.presentation;

import static com.greedy.zupzup.common.fixture.MemberFixture.MEMBER;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
import com.greedy.zupzup.quiz.exception.QuizException;
import com.greedy.zupzup.quiz.presentation.dto.AnswerRequest;
import com.greedy.zupzup.quiz.presentation.dto.QuizSubmissionRequest;
import com.greedy.zupzup.quiz.presentation.dto.QuizSubmissionResponse;
import com.greedy.zupzup.quiz.presentation.dto.QuizzesResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class QuizControllerTest extends ControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private LostItem lostItem;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MEMBER());
        Category category = givenElectronicsCategory();
        lostItem = givenLostItem(member, category);
    }

    @Nested
    @DisplayName("퀴즈 생성 API")
    class GetQuizzes {

        @Test
        void 퀴즈_조회에_성공하면_200_OK와_퀴즈_목록을_응답한다() {

            long ExistentLostItemId = lostItem.getId();
            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .param("memberId", member.getId())
                    .when()
                    .get("/api/lost-items/{lostItemId}/quizzes", ExistentLostItemId)
                    .then().log().all()
                    .extract();

            // then
            QuizzesResponse response = extract.as(QuizzesResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.quizzes()).hasSize(2);
                softly.assertThat(response.quizzes().get(0).question()).isNotBlank();
                softly.assertThat(response.quizzes().get(1).question()).isNotBlank();
            });
        }

        @Test
        void 존재하지_않는_분실물로_퀴즈를_조회하면_404_Not_Found를_응답한다() {

            // given
            long nonExistentLostItemId = 999L;

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .param("memberId", member.getId())
                    .when()
                    .get("/api/lost-items/{lostItemId}/quizzes", nonExistentLostItemId)
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(404);
            });
        }
    }

    @Nested
    @DisplayName("퀴즈 제출 API")
    class SubmitQuizzes {

        @Test
        void 정답을_제출하면_200_OK와_정답_결과를_응답한다() throws Exception {

            // given
            List<LostItemFeature> correctFeatures = lostItemFeatureRepository.findByLostItemId(lostItem.getId());
            List<AnswerRequest> correctAnswers = correctFeatures.stream()
                    .map(feature -> new AnswerRequest(feature.getFeature().getId(), feature.getSelectedOption().getId()))
                    .collect(Collectors.toList());
            QuizSubmissionRequest request = new QuizSubmissionRequest(correctAnswers);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", member.getId())
                    .body(objectMapper.writeValueAsString(request))
                    .when()
                    .post("/api/lost-items/{lostItemId}/quizzes", lostItem.getId())
                    .then().log().all()
                    .extract();

            // then
            QuizSubmissionResponse response = extract.as(QuizSubmissionResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.correct()).isTrue();
                softly.assertThat(response.detail()).isNotNull();
            });
        }

        @Test
        void 오답을_제출하면_200_OK와_오답_결과를_응답한다() throws Exception {

            // given
            long wrongOptionId = 99L;
            List<LostItemFeature> correctFeatures = lostItemFeatureRepository.findByLostItemId(lostItem.getId());
            List<AnswerRequest> incorrectAnswers = correctFeatures.stream()
                    .map(feature -> new AnswerRequest(feature.getFeature().getId(), wrongOptionId))
                    .collect(Collectors.toList());
            QuizSubmissionRequest request = new QuizSubmissionRequest(incorrectAnswers);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", member.getId())
                    .body(objectMapper.writeValueAsString(request))
                    .when()
                    .post("/api/lost-items/{lostItemId}/quizzes", lostItem.getId())
                    .then().log().all()
                    .extract();

            // then
            QuizSubmissionResponse response = extract.as(QuizSubmissionResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.correct()).isFalse();
                softly.assertThat(response.detail()).isNull();
            });
        }

        @Test
        void 이미_틀린_기록이_있으면_403_Forbidden을_응답한다() throws Exception {

            // given
            quizAttemptRepository.save(QuizAttempt.builder()
                    .member(member).lostItem(lostItem).isCorrect(false).build());

            List<AnswerRequest> dummyAnswers = List.of(
                    new AnswerRequest(1L, 1L),
                    new AnswerRequest(2L, 5L)
            );
            QuizSubmissionRequest request = new QuizSubmissionRequest(dummyAnswers);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .queryParam("memberId", member.getId())
                    .body(objectMapper.writeValueAsString(request))
                    .when()
                    .post("/api/lost-items/{lostItemId}/quizzes", lostItem.getId())
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(403);
                softly.assertThat(response.title()).isEqualTo(QuizException.QUIZ_ATTEMPT_LIMIT_EXCEEDED.getTitle());
            });
        }
    }
}
