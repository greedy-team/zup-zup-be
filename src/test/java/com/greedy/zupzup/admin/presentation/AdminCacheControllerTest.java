package com.greedy.zupzup.admin.presentation;


import com.greedy.zupzup.admin.presentation.exception.AdminException;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.common.fixture.SchoolAreaFixture;
import com.greedy.zupzup.global.config.CacheType;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class AdminCacheControllerTest extends ControllerTest {

    private static final String PASSWORD = "password";

    @Nested
    @DisplayName("학교 구역 조회 캐시 리프레시 API")
    class SchoolAreas {

        private Cache schoolAreaCache;
        private List<SchoolArea> schoolAreas;
        private Member admin;

        @BeforeEach
        void setUp() {
            admin = givenAdmin(PASSWORD);
            schoolAreas = givenSchoolAreas();
            schoolAreaCache = cacheManager.getCache(CacheType.ALL_SCHOOL_AREA.getCacheName());
            schoolAreaCache.clear();
            RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .get("/api/school-areas")
                    .then().log().all();
        }

        @Test
        void 학교_구역_데이터를_수정하고_케시를_리프레시하면_새로운_캐시가_적재_되어야_한다() {

            // given
            String cacheKey = "all";
            String accessToken = givenAccessToken(admin);
            SchoolArea extraArea = SchoolAreaFixture.STUDENT_HALL();
            schoolAreaRepository.save(extraArea);

            Cache.ValueWrapper beforeValue = schoolAreaCache.get(cacheKey);
            List<SchoolArea> beforeCachedData = (List<SchoolArea>) beforeValue.get();

            // when
            RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .cookie("access_token", accessToken)
                    .when()
                    .post("/api/admin/cache/school-areas/refresh")
                    .then().log().all()
                    .statusCode(204);

            // then
            Cache.ValueWrapper afterValue = schoolAreaCache.get(cacheKey);
            List<SchoolArea> afterCachedData = (List<SchoolArea>) afterValue.get();

            assertSoftly(softly -> {
                assertThat(beforeValue).isNotNull();
                assertThat(afterValue).isNotNull();
                assertThat(beforeCachedData).hasSize(schoolAreas.size());
                assertThat(afterCachedData).hasSize(schoolAreas.size() + 1);
                assertThat(afterCachedData.size()).isEqualTo(beforeCachedData.size() + 1);
            });
        }

    }


    @Nested
    @DisplayName("카테고리 조회 캐시 리프레시 API")
    class Categories {

        private Cache categoryCache;
        private List<Category> categories;
        private Member admin;

        @BeforeEach
        void setUp() {
            admin = givenAdmin(PASSWORD);
            Category walletCategory = givenWalletCategory();
            Category electronicsCategory = givenElectronicsCategory();
            categories = List.of(walletCategory, electronicsCategory);

            categoryCache = cacheManager.getCache(CacheType.ALL_CATEGORY.getCacheName());
            categoryCache.clear();

            RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .get("/api/categories")
                    .then().log().all();
        }

        @Test
        void 카테고리_데이터를_수정하고_케시를_리프레시하면_새로운_캐시가_적재_되어야_한다() {

            // given
            String cacheKey = "all";
            String accessToken = givenAccessToken(admin);
            Category extraCategory = givenEtcCategory();

            Cache.ValueWrapper beforeValue = categoryCache.get(cacheKey);
            CategoriesResponse beforeCachedData = (CategoriesResponse) beforeValue.get();

            // when
            RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .cookie("access_token", accessToken)
                    .when()
                    .post("/api/admin/cache/categories/refresh")
                    .then().log().all()
                    .statusCode(204);

            // then
            Cache.ValueWrapper afterValue = categoryCache.get(cacheKey);
            CategoriesResponse afterCachedData = (CategoriesResponse) afterValue.get();

            assertSoftly(softly -> {
                assertThat(beforeValue).isNotNull();
                assertThat(afterValue).isNotNull();
                assertThat(beforeCachedData.categories()).hasSize(categories.size());
                assertThat(afterCachedData.categories()).hasSize(categories.size() + 1);
            });
        }
    }

    @Nested
    @DisplayName("어드민 권한 테스트")
    class Admin {

        private Member member;
        private String accessToken;

        @BeforeEach
        void setUp() {
            member = givenMember(PASSWORD);
            accessToken = givenAccessToken(member);
        }

        @Test
        void 학교_구역_캐시_리프레시를_ADMIN_권한이_아닌_사용자가_호출하면_403_FORBIDEN이_발생해야_한다() {

            // given & when
            ErrorResponse response = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .cookie("access_token", accessToken)
                    .when()
                    .post("/api/admin/cache/school-areas/refresh")
                    .then().log().all()
                    .statusCode(403)
                    .extract()
                    .as(ErrorResponse.class);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.status()).isEqualTo(AdminException.FORBIDDEN_ADMIN_ACCESS.getHttpStatus().value());
                softly.assertThat(response.title()).isEqualTo(AdminException.FORBIDDEN_ADMIN_ACCESS.getTitle());
                softly.assertThat(response.detail()).isEqualTo(AdminException.FORBIDDEN_ADMIN_ACCESS.getDetail());
                softly.assertThat(response.instance()).isEqualTo("/api/admin/cache/school-areas/refresh");
            });
        }

        @Test
        void 카테고리_캐시_리프레시를_ADMIN_권한이_아닌_사용자가_호출하면_403_FORBIDEN이_발생해야_한다() {

            // given & when
            ErrorResponse response = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .cookie("access_token", accessToken)
                    .when()
                    .post("/api/admin/cache/categories/refresh")
                    .then().log().all()
                    .statusCode(403)
                    .extract()
                    .as(ErrorResponse.class);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.status()).isEqualTo(AdminException.FORBIDDEN_ADMIN_ACCESS.getHttpStatus().value());
                softly.assertThat(response.title()).isEqualTo(AdminException.FORBIDDEN_ADMIN_ACCESS.getTitle());
                softly.assertThat(response.detail()).isEqualTo(AdminException.FORBIDDEN_ADMIN_ACCESS.getDetail());
                softly.assertThat(response.instance()).isEqualTo("/api/admin/cache/categories/refresh");
            });
        }

    }

}
