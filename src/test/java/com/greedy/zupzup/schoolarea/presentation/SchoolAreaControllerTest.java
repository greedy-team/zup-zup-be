package com.greedy.zupzup.schoolarea.presentation;

import com.greedy.zupzup.common.ControllerTest;
import com.greedy.zupzup.global.exception.CommonException;
import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.exception.SchoolAreaException;
import com.greedy.zupzup.schoolarea.presentation.dto.AllSchoolAreasResponse;
import com.greedy.zupzup.schoolarea.presentation.dto.SchoolAreaResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class SchoolAreaControllerTest extends ControllerTest {

    private List<SchoolArea> schoolAreas;

    @BeforeEach
    void setUp() {
        schoolAreas = givenSchoolAreas();
    }

    @Nested
    @DisplayName("모든 학교 구역 조회")
    class findAll {
        @Test
        void 모든_학교_구역조회에_성공하면_200_OK를_응답해야_한다() {

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
            assertThat(response.count()).isEqualTo(schoolAreas.size());
        }
    }


    @Nested
    @DisplayName("위도/경도로 학교 구역 조회")
    class findArea {
        @Test
        void 위도_경도_좌표가_주어지면_해당_좌표가_속한_학교_구역과_200_OK를_응답해야_한다() {

            // given
            Double lat = 37.55121049168251;
            Double lng = 127.0745588999;
            SchoolArea expectedArea = schoolAreas.get(2);

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .param("lat", String.valueOf(lat))
                    .param("lng", String.valueOf(lng))
                    .when()
                    .get("/api/school-areas/contains")
                    .then().log().all()
                    .extract();

            // then
            int statusCode = extract.statusCode();
            SchoolAreaResponse response = extract.as(SchoolAreaResponse.class);

            assertSoftly(softly -> {
                softly.assertThat(statusCode).isEqualTo(200);
                softly.assertThat(response.id()).isEqualTo(expectedArea.getId());
                softly.assertThat(response.areaName()).isEqualTo(expectedArea.getAreaName());
            });
        }

        @Test
        void 주어진_위도_경도_좌표가_속한_학교_구역이_존재하지_않는다면_404_NOT_FOUND를_응답해야_한다() {

            // given
            Double lat = 0.0;
            Double lng = 0.0;

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .param("lat", String.valueOf(lat))
                    .param("lng", String.valueOf(lng))
                    .when()
                    .get("/api/school-areas/contains")
                    .then().log().all()
                    .extract();

            // then
            int statusCode = extract.statusCode();
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(statusCode).isEqualTo(404);
                softly.assertThat(response.status()).isEqualTo(SchoolAreaException.SCHOOL_AREA_OUT_OF_BOUND.getHttpStatus().value());
                softly.assertThat(response.title()).isEqualTo(SchoolAreaException.SCHOOL_AREA_OUT_OF_BOUND.getTitle());
                softly.assertThat(response.detail()).isEqualTo(SchoolAreaException.SCHOOL_AREA_OUT_OF_BOUND.getDetail());
                softly.assertThat(response.instance()).isEqualTo("/api/school-areas/contains");
            });
        }

        @Test
        void 주어진_위도_경도의_범위가_유효하지_않다면_400_BAD_REQUEST를_응답해야_한다() {

            // given - 위도는 -90 ~ 90 | 경도는 -180 ~ 180 이어야 한다.
            Double lat = 100.0;
            Double lng = -200.0;

            // when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .param("lat", String.valueOf(lat))
                    .param("lng", String.valueOf(lng))
                    .when()
                    .get("/api/school-areas/contains")
                    .then().log().all()
                    .extract();

            // then
            int statusCode = extract.statusCode();
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(statusCode).isEqualTo(400);
                softly.assertThat(response.status()).isEqualTo(CommonException.INVALID_INPUT_VALUE.getHttpStatus().value());
                softly.assertThat(response.title()).isEqualTo(CommonException.INVALID_INPUT_VALUE.getTitle());
                softly.assertThat(response.detail()).contains("경도는 -180 이상이어야 합니다.");
                softly.assertThat(response.detail()).contains("위도는 90 이하여야 합니다.");
                softly.assertThat(response.instance()).isEqualTo("/api/school-areas/contains");
            });
        }

        @Test
        void 위도_경도의_값이_주어지지_않는_다면_400_BAD_REQUEST를_응답해야_한다() {

            // given & when
            ExtractableResponse<Response> extract = RestAssured.given().log().all()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                    .get("/api/school-areas/contains")
                    .then().log().all()
                    .extract();

            // then
            int statusCode = extract.statusCode();
            ErrorResponse response = extract.as(ErrorResponse.class);
            assertSoftly(softly -> {
                softly.assertThat(statusCode).isEqualTo(400);
                softly.assertThat(response.status()).isEqualTo(CommonException.INVALID_INPUT_VALUE.getHttpStatus().value());
                softly.assertThat(response.title()).isEqualTo(CommonException.INVALID_INPUT_VALUE.getTitle());
                softly.assertThat(response.instance()).isEqualTo("/api/school-areas/contains");
            });
        }
    }
}
