package com.greedy.zupzup.schoolarea.presentation;

import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.schoolarea.presentation.dto.AllSchoolAreasResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.*;

class SchoolAreaControllerTest extends ControllerTest {

    @Test
    void 모든_학교_구역을_응답해야한다() {

        // given & when
        AllSchoolAreasResponse response = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get("/api/school-areas")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AllSchoolAreasResponse.class);

        // then
        assertThat(response.count()).isEqualTo(3);
    }


}
