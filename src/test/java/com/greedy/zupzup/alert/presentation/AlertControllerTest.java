package com.greedy.zupzup.alert.presentation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greedy.zupzup.alert.domain.KeywordAlert;
import com.greedy.zupzup.alert.presentation.dto.SubscriptionResponse;
import com.greedy.zupzup.alert.presentation.dto.SubscriptionUpdateRequest;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.member.domain.Member;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AlertControllerTest extends ControllerTest {

    private static final String TEST_PASSWORD = "password1234";

    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private String accessToken;
    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        member = givenMemberWithEmail(TEST_PASSWORD);
        accessToken = givenAccessToken(member);
        category1 = givenElectronicsCategory();
        category2 = givenWalletCategory();
    }

    @Nested
    @DisplayName("구독 카테고리 조회 API")
    class GetSubscriptions {

        @Test
        void 구독_목록_조회에_성공하면_200_OK와_카테고리_ID_목록을_응답한다() {
            // given
            keywordAlertRepository.save(KeywordAlert.subscribe(member, category1));
            keywordAlertRepository.save(KeywordAlert.subscribe(member, category2));

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .cookie("access_token", accessToken)
                    .when()
                    .get("/api/alerts/subscriptions")
                    .then().log().all()
                    .extract();

            // then
            SubscriptionResponse response = extract.as(SubscriptionResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.categoryIds()).hasSize(2);
                softly.assertThat(response.categoryIds()).containsExactlyInAnyOrder(category1.getId(), category2.getId());
            });
        }
    }

    @Nested
    @DisplayName("구독 카테고리 수정 API")
    class UpdateSubscriptions {

        @Test
        void 구독_목록_수정에_성공하면_200_OK를_응답한다() throws Exception {
            // given
            List<Long> newCategoryIds = List.of(category1.getId());
            SubscriptionUpdateRequest request = new SubscriptionUpdateRequest(newCategoryIds);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", accessToken)
                    .body(objectMapper.writeValueAsString(request))
                    .when()
                    .put("/api/alerts/subscriptions")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
            });
        }

        @Test
        void 카테고리_ID_목록이_누락되면_400_Bad_Request를_응답한다() throws Exception {
            // given
            SubscriptionUpdateRequest request = new SubscriptionUpdateRequest(null);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(ContentType.JSON)
                    .cookie("access_token", accessToken)
                    .body(objectMapper.writeValueAsString(request))
                    .when()
                    .put("/api/alerts/subscriptions")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(400);
            });
        }
    }
}
