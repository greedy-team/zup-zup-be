package com.greedy.zupzup.category.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.category.presentation.dto.CategoryFeaturesResponse;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.global.config.CacheType;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;


class CategoryControllerTest extends ControllerTest {

    private Category electronics;
    private Category wallet;
    private Cache allCategoryCache;
    private Cache categoryDetailsCache;

    @BeforeEach
    void setUp() {
        electronics = givenElectronicsCategory();
        wallet = givenWalletCategory();
        allCategoryCache = cacheManager.getCache(CacheType.ALL_CATEGORY.getCacheName());
        categoryDetailsCache = cacheManager.getCache(CacheType.CATEGORY_DETAILS.getCacheName());
        allCategoryCache.clear();
        categoryDetailsCache.clear();
    }

    @Nested
    @DisplayName("카테고리 전체 조회 API")
    class GetAll {

        @Test
        void 카테고리를_전체_조회하면_200_OK와_카테고리_목록을_응답해야_한다() {
            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/categories")
                    .then().log().all()
                    .extract();

            // then
            CategoriesResponse response = extract.as(CategoriesResponse.class);

            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);
                softly.assertThat(response.categories()).isNotEmpty();

                Set<String> names = response.categories().stream()
                        .map(c -> c.name())
                        .collect(Collectors.toSet());
                softly.assertThat(names).contains(electronics.getName(), wallet.getName());

                softly.assertThat(response.categories().get(0).id()).isNotNull();
                softly.assertThat(response.categories().get(0).name()).isNotBlank();
                softly.assertThat(response.categories().get(0).iconUrl()).isNotBlank();
            });
        }

        @Test
        void 두_번_이상_카테고리_전체를_조회하면_캐싱된_데이터를_응답해야_한다() {
            // given
            String cacheKey = "all";
            assertThat(allCategoryCache.get(cacheKey)).isNull();

            com.github.benmanes.caffeine.cache.Cache caffeineCache
                    = (com.github.benmanes.caffeine.cache.Cache) allCategoryCache.getNativeCache();
            long initialHitCount = caffeineCache.stats().hitCount();


            // when
            CategoriesResponse firstResponse = RestAssured.given().log().all()
                    .when()
                    .get("/api/categories")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .as(CategoriesResponse.class);

            CategoriesResponse secondResponse = RestAssured.given().log().all()
                    .when()
                    .get("/api/categories")
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .as(CategoriesResponse.class);

            // then
            long afterHitCount = caffeineCache.stats().hitCount();

            assertSoftly(softly -> {
                assertThat(secondResponse.categories().size()).isEqualTo(firstResponse.categories().size());
                assertThat(secondResponse.categories()).containsAll(firstResponse.categories());
                assertThat(afterHitCount).isEqualTo(initialHitCount + 1L);
                assertThat(allCategoryCache.get(cacheKey)).isNotNull();
            });
        }

    }

    @Nested
    @DisplayName("카테고리 별 전체 특징/옵션 조회 API")
    class GetCategoryFeaturesAndOptions {

        @Test
        void 카테고리ID로_특징과_옵션을_조회하면_200_OK와_목록을_응답해야_한다() {
            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/categories/{categoryId}/features", electronics.getId())
                    .then().log().all()
                    .extract();

            // then
            CategoryFeaturesResponse response = extract.as(CategoryFeaturesResponse.class);

            assertSoftly(softly -> {
                softly.assertThat(extract.statusCode()).isEqualTo(200);

                softly.assertThat(response.categoryId()).isEqualTo(electronics.getId());
                softly.assertThat(response.categoryName()).isEqualTo(electronics.getName());

                softly.assertThat(response.features()).isNotEmpty();
                response.features().forEach(f -> {
                    assertThat(f.id()).isNotNull();
                    assertThat(f.name()).isNotBlank();
                    assertThat(f.quizQuestion()).isNotBlank();
                    assertThat(f.options()).isNotEmpty();

                    int last = f.options().size() - 1;
                    assertThat(f.options().get(0).id()).isNotNull();
                    assertThat(f.options().get(0).optionValue()).isNotBlank();
                });
            });
        }

        @Test
        void 존재하지_않는_카테고리ID로_요청하면_404_Not_Found를_응답해야_한다() {
            long nonExistentId = 999_999L;

            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/categories/{categoryId}/features", nonExistentId)
                    .then().log().all()
                    .extract();

            assertSoftly(softly -> softly.assertThat(extract.statusCode()).isEqualTo(404));
        }

        @Test
        void 숫자가_아닌_카테고리ID면_400_BAD_REQUEST를_응답해야_한다() {
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .when()
                    .get("/api/categories/{categoryId}/features", "abc")
                    .then().log().all()
                    .extract();

            assertThat(extract.statusCode()).isEqualTo(400);
        }

        @Test
        void 두_번_이상_카테고리_특징옵션을_조회하면_캐싱된_데이터를_응답해야_한다() {
            // given
            Long categoryId = electronics.getId();
            Long cacheKey = categoryId;

            assertThat(categoryDetailsCache.get(cacheKey)).isNull();

            // when
            CategoryFeaturesResponse firstResponse = RestAssured.given().log().all()
                    .when()
                    .get("/api/categories/{categoryId}/features", categoryId)
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .as(CategoryFeaturesResponse.class);

            CategoryFeaturesResponse secondResponse = RestAssured.given().log().all()
                    .when()
                    .get("/api/categories/{categoryId}/features", categoryId)
                    .then().log().all()
                    .statusCode(200)
                    .extract()
                    .as(CategoryFeaturesResponse.class);

            // then
            com.github.benmanes.caffeine.cache.Cache caffeineCache
                    = (com.github.benmanes.caffeine.cache.Cache) categoryDetailsCache.getNativeCache();

            assertSoftly(softly -> {
                assertThat(secondResponse.categoryId()).isEqualTo(firstResponse.categoryId());
                assertThat(secondResponse.features()).containsAll(firstResponse.features());
                assertThat(caffeineCache.stats().hitCount()).isEqualTo(1L);
                assertThat(categoryDetailsCache.get(cacheKey)).isNotNull();
            });
        }

    }

}
