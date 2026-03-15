package com.greedy.zupzup.member.presentation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.presentation.dto.MemberEmailUpdateRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberControllerTest extends ControllerTest {

    private static final String TEST_PASSWORD = "password1234";

    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private String accessToken;

    @BeforeEach
    void setUp() {
        member = givenMember(TEST_PASSWORD);
        accessToken = givenAccessToken(member);
    }

    @Nested
    @DisplayName("이메일 정보 조회 API")
    class GetEmailInfo {

        @Test
        void 이메일_정보_조회에_성공하면_200_OK와_이메일_정보를_응답한다() {
            // given
            member.updateEmailInfo("test@sju.ac.kr", true);
            memberRepository.save(member);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .cookie("access_token", accessToken)
                    .when()
                    .get("/api/members/me/email")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(extract.jsonPath().getString("email")).isEqualTo("test@sju.ac.kr");
                softly.assertThat(extract.jsonPath().getBoolean("emailAlertEnabled")).isTrue();
            });
        }

        @Test
        void 인증_정보_없이_요청하면_401_Unauthorized를_응답한다() {
            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/members/me/email")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(401);
            });
        }
    }

    @Nested
    @DisplayName("이메일 정보 수정 API")
    class UpdateEmail {

        @Test
        void 이메일_정보_수정에_성공하면_200_OK를_응답한다() throws Exception {
            // given
            MemberEmailUpdateRequest request = new MemberEmailUpdateRequest("new@example.com", true);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", accessToken)
                    .body(objectMapper.writeValueAsString(request))
                    .when()
                    .put("/api/members/me/email")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
            });
        }

        @Test
        void 잘못된_이메일_형식으로_요청하면_400_Bad_Request를_응답한다() throws Exception {
            // given
            MemberEmailUpdateRequest request = new MemberEmailUpdateRequest("invalid-email", true);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", accessToken)
                    .body(objectMapper.writeValueAsString(request))
                    .when()
                    .put("/api/members/me/email")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(400);
            });
        }

        @Test
        void 인증_정보_없이_요청하면_401_Unauthorized를_응답한다() throws Exception {
            // given
            MemberEmailUpdateRequest request = new MemberEmailUpdateRequest("test@example.com", true);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .body(objectMapper.writeValueAsString(request))
                    .when()
                    .put("/api/members/me/email")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(401);
            });
        }
    }
}
