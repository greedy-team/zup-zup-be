package com.greedy.zupzup.lostitem.presentation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemSummaryResponse;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LostItemSummaryControllerTest extends ControllerTest {

    private Member owner;
    private Category category;
    private List<SchoolArea> areas;

    @BeforeEach
    void setUp() {
        owner = givenMember("pw!23456");
        category = givenElectronicsCategory();
        areas = givenSchoolAreas();

        for (SchoolArea area : areas) {
            givenLostItem(owner, category);
        }
    }

    @Test
    void 구역별_요약_조회는_200_OK와_카운트를_응답한다() {
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .when()
                .get("/api/lost-items/summary")
                .then().log().all()
                .extract();

        LostItemSummaryResponse response = extract.as(LostItemSummaryResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(200);
            softly.assertThat(response.areas()).isNotEmpty();
            softly.assertThat(response.areas().size()).isGreaterThanOrEqualTo(areas.size());
        });
    }

    @Test
    void 카테고리_필터를_적용해도_200_OK_를_응답한다() {
        ExtractableResponse<Response> extract = RestAssured.given().log().all()
                .queryParam("categoryId", category.getId())
                .when()
                .get("/api/lost-items/summary")
                .then().log().all()
                .extract();

        LostItemSummaryResponse response = extract.as(LostItemSummaryResponse.class);

        assertSoftly(softly -> {
            softly.assertThat(extract.statusCode()).isEqualTo(200);
            softly.assertThat(response.areas()).isNotEmpty();
        });
    }
}
