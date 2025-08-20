package com.greedy.zupzup.pledge.presentation;

import static com.greedy.zupzup.common.fixture.MemberFixture.MEMBER;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.pledge.presentation.dto.PledgeResponse;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
import com.greedy.zupzup.quiz.exception.QuizException;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PledgeControllerTest extends ControllerTest {

    private Member member;
    private LostItem quizLostItem;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MEMBER());
        Category category = givenElectronicsCategory();
        quizLostItem = givenLostItem(member, category);
    }

    @Nested
    @DisplayName("서약 생성 API")
    class CreatePledge {

        @Test
        void 퀴즈를_통과한_경우_서약_작성에_성공하고_201_Created를_응답한다() {
            // given
            quizAttemptRepository.save(QuizAttempt.builder()
                    .member(member).lostItem(quizLostItem).isCorrect(true).build());

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .queryParam("memberId", member.getId())
                    .when()
                    .post("/api/lost-items/{lostItemId}/pledge", quizLostItem.getId())
                    .then().log().all()
                    .extract();

            // then
            PledgeResponse response = extract.as(PledgeResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(201);
                softly.assertThat(response.pledgeId()).isNotNull();
            });
        }

        @Test
        void 퀴즈가_없는_분실물은_서약_작성에_성공하고_201_Created를_응답한다() {

            // given
            Category etcCategory = givenEtcCategory();
            LostItem nonQuizLostItem = givenNonQuizLostItem(member, etcCategory);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .queryParam("memberId", member.getId())
                    .when()
                    .post("/api/lost-items/{lostItemId}/pledge", nonQuizLostItem.getId())
                    .then().log().all()
                    .extract();

            // then
            PledgeResponse response = extract.as(PledgeResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(201);
                softly.assertThat(response.pledgeId()).isNotNull();
            });
        }

        @Test
        void 이미_서약_작성된_분실물은_409_Conflict를_응답한다() {

            // given
            quizLostItem.pledge();
            lostItemRepository.save(quizLostItem);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .queryParam("memberId", member.getId())
                    .when()
                    .post("/api/lost-items/{lostItemId}/pledge", quizLostItem.getId())
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(409);
                softly.assertThat(response.title()).isEqualTo(LostItemException.ALREADY_PLEDGED.getTitle());
            });
        }

        @Test
        void 퀴즈를_풀지_않은_경우_400_Bad_Request를_응답한다() {

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .queryParam("memberId", member.getId())
                    .when()
                    .post("/api/lost-items/{lostItemId}/pledge", quizLostItem.getId())
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(400);
                softly.assertThat(response.title()).isEqualTo(QuizException.QUIZ_NOT_ATTEMPTED.getTitle());
            });
        }

        @Test
        void 퀴즈를_틀린_경우_403_Forbidden를_응답한다() {

            // given
            quizAttemptRepository.save(QuizAttempt.builder()
                    .member(member).lostItem(quizLostItem).isCorrect(false).build());

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .queryParam("memberId", member.getId())
                    .when()
                    .post("/api/lost-items/{lostItemId}/pledge", quizLostItem.getId())
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
