package com.greedy.zupzup.lostitem.presentation;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemSummaryResponse;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemSummaryResponse.AreaSummary;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
            givenLostItemInArea(owner, category, area);
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
    void 카테고리_필터가_적용되면_해당_카테고리만_카운트된다() {
        Category other = givenWalletCategory();

        for (SchoolArea area : areas) {
            givenLostItemInArea(owner, other, area);
            givenLostItemInArea(owner, other, area);
        }

        LostItemSummaryResponse all = RestAssured.given()
                .when().get("/api/lost-items/summary")
                .then().extract().as(LostItemSummaryResponse.class);

        LostItemSummaryResponse filtered = RestAssured.given()
                .queryParam("categoryId", category.getId())
                .when().get("/api/lost-items/summary")
                .then().extract().as(LostItemSummaryResponse.class);

        Map<Long, Long> allMap = all.areas().stream()
                .collect(Collectors.toMap(AreaSummary::schoolAreaId, AreaSummary::lostCount));
        Map<Long, Long> filteredMap = filtered.areas().stream()
                .collect(Collectors.toMap(AreaSummary::schoolAreaId, AreaSummary::lostCount));

        assertSoftly(softly -> {
            softly.assertThat(filtered.areas()).isNotEmpty();
            for (SchoolArea area : areas) {
                Long id = area.getId();
                softly.assertThat(filteredMap).containsKey(id);
                softly.assertThat(filteredMap.get(id)).isEqualTo(1L);
                softly.assertThat(allMap.get(id)).isEqualTo(3L);
            }
        });
    }
}
