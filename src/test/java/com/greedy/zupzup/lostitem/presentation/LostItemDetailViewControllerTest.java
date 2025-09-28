package com.greedy.zupzup.lostitem.presentation;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.common.fixture.PledgeFixture;
import com.greedy.zupzup.common.fixture.QuizAttemptFixture;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;

import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.pledge.domain.Pledge;
import com.greedy.zupzup.pledge.repository.PledgeRepository;
import com.greedy.zupzup.quiz.domain.QuizAttempt;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

class LostItemDetailViewControllerTest extends ControllerTest {

    private static final String ACCESS_COOKIE = "access_token";

    @Autowired
    PledgeRepository pledgeRepository;

    private Member member;
    private String accessToken;

    @BeforeEach
    void initMember() {
        member = givenMember("password123!");
        accessToken = givenAccessToken(member);
    }

    private LostItem persistElectronicLostItemGraph() {
        Category electronics = givenElectronicsCategory();
        return givenRegisteredLostItem(electronics);
    }

    private void saveQuizAttempt(LostItem item, boolean correct) {
        QuizAttempt qa = correct
                ? QuizAttemptFixture.CORRECT_QUIZ_ATTEMPT(member, item)
                : QuizAttemptFixture.INCORRECT_QUIZ_ATTEMPT(member, item);
        quizAttemptRepository.save(qa);
    }

    private void savePledge(LostItem item) {
        Pledge pledge = PledgeFixture.PLEDGE(member, item);
        pledgeRepository.save(pledge);

        ReflectionTestUtils.setField(item, "status", LostItemStatus.PLEDGED);
        ReflectionTestUtils.setField(item, "pledgedAt", LocalDate.now());
        lostItemRepository.save(item);
    }

    private ExtractableResponse<Response> get(String path) {
        return given().log().all()
                .cookie(ACCESS_COOKIE, accessToken)
                .when()
                .get(path)
                .then().log().all()
                .extract();
    }


    @Nested
    @DisplayName("분실물 사진 + 상세 정보(퀴즈 이후) API")
    class ImagesAfterQuizApi {

        @Test
        void 퀴즈_통과_서약_완료면_200_OK를_응답_한다() {
            LostItem item = persistElectronicLostItemGraph();
            saveQuizAttempt(item, true);
            savePledge(item);

            ExtractableResponse<Response> res = get("/api/lost-items/" + item.getId() + "/image");
            assertThat(res.statusCode()).isEqualTo(200);
        }

        @Test
        void 퀴즈미통과면_403_Forbidden을_응답한다() {
            LostItem item = persistElectronicLostItemGraph();
            saveQuizAttempt(item, false);
            savePledge(item);

            ExtractableResponse<Response> res = get("/api/lost-items/" + item.getId() + "/image");
            assertThat(res.statusCode()).isEqualTo(403);
        }
    }

    @Nested
    @DisplayName("보관 위치 조회 API")
    class DepositAreaApi {

        @Test
        void 서약완료_퀴즈통과면_200_OK_와_보관위치를_응답한다() {
            LostItem item = persistElectronicLostItemGraph();
            saveQuizAttempt(item, true);
            savePledge(item);

            ExtractableResponse<Response> res = get("/api/lost-items/" + item.getId() + "/deposit-area");
            assertThat(res.statusCode()).isEqualTo(200);
            assertThat(res.jsonPath().getString("depositArea")).isEqualTo("학술정보원 2층 데스크");
        }

        @Test
        void 존재하지않는_ID면_404NotFound를_응답한다() {
            ExtractableResponse<Response> res = get("/api/lost-items/999999/deposit-area");
            assertThat(res.statusCode()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("상세 조회 (보관 장소 포함) API")
    class DetailApi {

        @Test
        void 서약완료한_퀴즈통과자면_200OK를_응답한다() {
            LostItem item = persistElectronicLostItemGraph();
            saveQuizAttempt(item, true);
            savePledge(item);

            ExtractableResponse<Response> res = get("/api/lost-items/" + item.getId() + "/detail");
            assertThat(res.statusCode()).isEqualTo(200);
        }

        @Test
        void 퀴즈미통과_시_403Forbidden을_응답한다() {
            LostItem item = persistElectronicLostItemGraph();
            saveQuizAttempt(item, false);

            ExtractableResponse<Response> res = get("/api/lost-items/" + item.getId() + "/detail");
            assertThat(res.statusCode()).isEqualTo(403);
        }

        @Test
        void 서약완료하지않은_퀴즈_통과자_403Forbidden을_응답한다(){
            LostItem item = persistElectronicLostItemGraph();
            saveQuizAttempt(item, true);

            ExtractableResponse<Response> res = get("/api/lost-items/" + item.getId() + "/detail");
            assertThat(res.statusCode()).isEqualTo(403);
        }
    }
}
