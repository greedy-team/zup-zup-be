package com.greedy.zupzup.lostitem.presentation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemViewResponse;
import com.greedy.zupzup.member.domain.Member;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class LostItemViewControllerTest extends ControllerTest {

    private Member owner;
    private Category category;
    private LostItem anyItem;

    @BeforeEach
    void setUpData() {
        owner = givenMember("pw123456!");
        category = givenElectronicsCategory();
        anyItem = givenLostItem(owner, category);

        IntStream.range(0, 4).forEach(i -> givenLostItem(owner, category));
    }

    @Nested
    @DisplayName("분실물 목록 조회 API")
    class ListApi {

        @Test
        void 목록_조회에_성공하면_200_OK와_리스트를_응답한다() {
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .queryParam("page", 1)
                    .queryParam("limit", 10)
                    .when()
                    .get("/api/lost-items")
                    .then().log().all()
                    .extract();

            LostItemListResponse response = extract.as(LostItemListResponse.class);

            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.count()).isPositive();
                softly.assertThat(response.items()).isNotEmpty();
                softly.assertThat(response.items().get(0).representativeImageUrl()).isNotBlank();
            });
        }

        @Test
        void 카테고리_이름이_기타면_목록_대표이미지는_0번_사진_URL_200_OK를_응답한다() {
            // given
            Category etc = givenEtcCategory();
            ReflectionTestUtils.setField(etc, "iconUrl", "");
            categoryRepository.saveAndFlush(etc);

            LostItem item = givenNonQuizLostItem(owner, etc);
            Long id = item.getId();

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .queryParam("page", 1)
                    .queryParam("limit", 50)
                    .when()
                    .get("/api/lost-items")
                    .then().log().all()
                    .extract();

            LostItemListResponse response = extract.as(LostItemListResponse.class);

            // then
            String expected = "https://example.com/default-image.jpg";
            var target = response.items().stream()
                    .filter(v -> v.id().equals(id))
                    .findFirst()
                    .orElseThrow();

            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(target.representativeImageUrl()).isEqualTo(expected);
            });
        }

        @Test
        void 목록_조회_카테고리_필터링이_적용된다() {
            Category wallet = givenWalletCategory();
            LostItem w1 = givenNonQuizLostItem(owner, wallet);
            LostItem w2 = givenNonQuizLostItem(owner, wallet);
            LostItem w3 = givenNonQuizLostItem(owner, wallet);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .queryParam("page", 1)
                    .queryParam("limit", 50)
                    .queryParam("categoryId", wallet.getId())
                    .when()
                    .get("/api/lost-items")
                    .then().log().all()
                    .extract();

            LostItemListResponse response = extract.as(LostItemListResponse.class);

            // then
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.items()).isNotEmpty();
                boolean allMatch = response.items().stream()
                        .allMatch(i -> i.categoryId().equals(wallet.getId()));
                softly.assertThat(allMatch).isTrue();
                softly.assertThat(response.count()).isEqualTo(3);
                softly.assertThat(response.items().size()).isEqualTo(3);
            });
        }

        @Test
        void 목록_조회_limit가_50초과면_400을_응답한다() {
            RestAssured.given()
                    .queryParam("page", 1)
                    .queryParam("limit", 51)
                    .when()
                    .get("/api/lost-items")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("분실물 단건(간단) 조회 API")
    class BasicApi {

        @Test
        void 단건_조회에_성공하면_200_OK와_데이터를_응답한다() {
            Long id = anyItem.getId();

            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/lost-items/{id}", id)
                    .then().log().all()
                    .extract();

            LostItemViewResponse response = extract.as(LostItemViewResponse.class);

            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.id()).isEqualTo(id);
                softly.assertThat(response.categoryName()).isNotBlank();
                softly.assertThat(response.representativeImageUrl()).isNotBlank();
            });
        }

        @Test
        void 존재하지_않는_분실물은_404_Not_Found를_응답한다() {
            long nonExistent = 999_999L;

            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/lost-items/{id}", nonExistent)
                    .then().log().all()
                    .extract();

            assertSoftly(softly -> softly.assertThat(extract.statusCode()).isEqualTo(404));
        }

        @Test
        void 카테고리_이름이_기타면_단건_대표이미지는_0번_사진_URL_200_OK를_응답한다() {
            // given
            Category etc = givenEtcCategory();
            ReflectionTestUtils.setField(etc, "iconUrl", "https://icon.com/etc.svg");
            categoryRepository.saveAndFlush(etc);

            LostItem item = givenNonQuizLostItem(owner, etc);
            Long id = item.getId();

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/lost-items/{id}", id)
                    .then().log().all()
                    .extract();

            LostItemViewResponse response = extract.as(LostItemViewResponse.class);

            // then
            String expected = "https://example.com/default-image.jpg";
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.id()).isEqualTo(id);
                softly.assertThat(response.representativeImageUrl()).isEqualTo(expected);
            });
        }

        @Test
        void 단건_조회_PLEDGED면_403_FORBIDDEN를_응답한다() {
            // given
            LostItem item = givenLostItem(owner, category);
            Long id = item.getId();
            ReflectionTestUtils.setField(item, "status", LostItemStatus.PLEDGED);
            lostItemRepository.saveAndFlush(item);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/lost-items/{id}", id)
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse error = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(403);
                softly.assertThat(error.title()).isEqualTo(LostItemException.ACCESS_FORBIDDEN.getTitle());
                softly.assertThat(error.detail()).contains("서약 진행 중");
            });
        }

        @Test
        void 단건_조회_FOUND면_403_FORBIDDEN를_응답한다() {
            // given
            LostItem item = givenLostItem(owner, category);
            Long id = item.getId();
            ReflectionTestUtils.setField(item, "status", LostItemStatus.FOUND);
            lostItemRepository.saveAndFlush(item);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/lost-items/{id}", id)
                    .then().log().all()
                    .extract();

            // then
            ErrorResponse error = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(403);
                softly.assertThat(error.title()).isEqualTo(LostItemException.ACCESS_FORBIDDEN.getTitle());
                softly.assertThat(error.detail()).contains("주인이 찾아간");
            });
        }
    }
}
