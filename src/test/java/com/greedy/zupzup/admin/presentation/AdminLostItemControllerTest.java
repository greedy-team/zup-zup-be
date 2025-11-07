package com.greedy.zupzup.admin.presentation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.common.fixture.MemberFixture;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.admin.lostitem.presentation.dto.ApproveLostItemsRequest;
import com.greedy.zupzup.admin.lostitem.presentation.dto.RejectLostItemsRequest;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Role;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class AdminLostItemControllerTest extends ControllerTest {

    private static final String ACCESS_TOKEN_NAME = "access_token";
    private Member adminMember;
    private String adminToken;
    private Category category;

    @BeforeEach
    void setUpAdmin() {
        Member adminBase = MemberFixture.MEMBER_WITH_ENCODED_PASSWORD("admin_pw");
        ReflectionTestUtils.setField(adminBase, "studentId", 800000);
        ReflectionTestUtils.setField(adminBase, "role", Role.ADMIN);
        adminMember = memberRepository.save(adminBase);
        adminToken = jwtTokenProvider.createAccessToken(adminMember);

        category = givenElectronicsCategory();
    }

    @Nested
    @DisplayName("관리자 분실물 일괄 처리 API")
    class AdminBulkApi {

        private final String ADMIN_API_BASE = "/api/admin/lost-items";

        @Test
        void 여러_개의_보류_분실물의_상태를_REGISTERED로_바꾼다() {
            // given
            LostItem i1 = givenPendingLostItem(category);
            LostItem i2 = givenPendingLostItem(category);
            List<Long> idsToApprove = List.of(i1.getId(), i2.getId());

            ApproveLostItemsRequest request = new ApproveLostItemsRequest(idsToApprove);

            // when
            ExtractableResponse<Response> extract = io.restassured.RestAssured.given().log().all()
                    .cookie(ACCESS_TOKEN_NAME, adminToken)
                    .contentType("application/json")
                    .body(request)
                    .when().post(ADMIN_API_BASE + "/approve")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(extract.jsonPath().getInt("successfulCount")).isEqualTo(2);

                softly.assertThat(lostItemRepository.findById(i1.getId()).get().getStatus())
                        .isEqualTo(LostItemStatus.REGISTERED);
            });
        }

        @Test
        void 한_개_보류_분실물의_상태를_REGISTERED로_바꾼다() {
            // given
            LostItem i1 = givenPendingLostItem(category);
            List<Long> idsToApprove = List.of(i1.getId());

            ApproveLostItemsRequest request = new ApproveLostItemsRequest(idsToApprove);

            // when
            ExtractableResponse<Response> extract = io.restassured.RestAssured.given().log().all()
                    .cookie(ACCESS_TOKEN_NAME, adminToken)
                    .contentType("application/json")
                    .body(request)
                    .when().post(ADMIN_API_BASE + "/approve")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(extract.jsonPath().getInt("successfulCount")).isEqualTo(1);

                softly.assertThat(lostItemRepository.findById(i1.getId()).get().getStatus())
                        .isEqualTo(LostItemStatus.REGISTERED);
            });
        }

        @Test
        void 여러_개의_분실물을_DB에서_삭제한다() {
            // given
            LostItem i1 = givenRegisteredLostItem(category);
            LostItem i2 = givenRegisteredLostItem(category);
            List<Long> idsToDelete = List.of(i1.getId(), i2.getId());

            RejectLostItemsRequest request = new RejectLostItemsRequest(idsToDelete);

            Mockito.doNothing().when(imageFileManager).delete(Mockito.anyString());

            // when
            ExtractableResponse<Response> extract = io.restassured.RestAssured.given().log().all()
                    .cookie(ACCESS_TOKEN_NAME, adminToken)
                    .contentType("application/json")
                    .body(request)
                    .when().post(ADMIN_API_BASE + "/reject")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);

                softly.assertThat(extract.jsonPath().getInt("successfulCount")).isEqualTo(2);

                softly.assertThat(lostItemRepository.findById(i1.getId())).isEmpty();
                softly.assertThat(lostItemRepository.findById(i2.getId())).isEmpty();
            });

            Mockito.verify(imageFileManager, Mockito.times(2)).delete(Mockito.anyString());
        }

        @Test
        void 한_개의_분실물을_DB에서_삭제한다() {
            // given
            LostItem i1 = givenRegisteredLostItem(category);
            List<Long> idsToDelete = List.of(i1.getId());

            RejectLostItemsRequest request = new RejectLostItemsRequest(idsToDelete);

            Mockito.doNothing().when(imageFileManager).delete(Mockito.anyString());

            // when
            ExtractableResponse<Response> extract = io.restassured.RestAssured.given().log().all()
                    .cookie(ACCESS_TOKEN_NAME, adminToken)
                    .contentType("application/json")
                    .body(request)
                    .when().post(ADMIN_API_BASE + "/reject")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);

                softly.assertThat(extract.jsonPath().getInt("successfulCount")).isEqualTo(1);

                softly.assertThat(lostItemRepository.findById(i1.getId())).isEmpty();
            });

            Mockito.verify(imageFileManager, Mockito.times(1)).delete(Mockito.anyString());
        }
    }

    @Nested
    @DisplayName("관리자 분실물 목록 조회 API")
    class AdminListApi {

        @Test
        void 보류_상태_분실물이_조회된다() {
            // given
            LostItem i1 = givenPendingLostItemWithFeatures(category);
            givenLostItemImages(i1.getId(), List.of("imgA", "imgB", "imgC"));

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .cookie(ACCESS_TOKEN_NAME, adminToken)
                    .queryParam("page", 1)
                    .queryParam("limit", 10)
                    .when().get("/api/admin/lost-items/pending")
                    .then().log().all()
                    .extract();

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);

                List<String> urls = extract.jsonPath()
                        .getList("items.find{it.id == " + i1.getId() + "}.imageUrl");
                softly.assertThat(urls).containsExactlyInAnyOrder("imgA", "imgB", "imgC");

                List<String> features = extract.jsonPath()
                        .getList("items.find{it.id == " + i1.getId() + "}.featureOptions.optionValue");
                softly.assertThat(features).contains("삼성", "블랙");

                List<String> quizQuestions = extract.jsonPath()
                        .getList("items.find{it.id == " + i1.getId() + "}.featureOptions.quizQuestion");
                softly.assertThat(quizQuestions).contains("어떤 브랜드의 제품인가요?", "제품의 색상은 무엇인가요?");
            });
        }
    }
}
