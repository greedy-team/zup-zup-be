package com.greedy.zupzup.schoolarea.presentation;

import com.greedy.zupzup.global.exception.ErrorResponse;
import com.greedy.zupzup.schoolarea.presentation.dto.AllSchoolAreasResponse;
import com.greedy.zupzup.schoolarea.presentation.dto.LatLngRequest;
import com.greedy.zupzup.schoolarea.presentation.dto.SchoolAreaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;


@Tag(name = "SchoolArea", description = "학교 구역 조회 관련 API")
public interface SchoolAreaControllerDocs {


    @Operation(summary = "좌표(위도,경도)로 세종대학교 구역 조회",
            description = "입력한 위도(lat)와 경도(lng)를 포함하는 학교 구역 정보를 응답합니다."
    )
    @Parameters({
            @Parameter(name = "lat", description = "위도", example = "37.5488", required = true),
            @Parameter(name = "lng", description = "경도", example = "127.0737", required = true)
    })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "요청한 좌표를 포함하는 학교 구역 정보 조회에 성공한 경우",
                    content = @Content(
                            schema = @Schema(implementation = SchoolAreaResponse.class),
                            examples = @ExampleObject(
                                    name = "좌표를 포함하는 학교 구역 정보 조회 성공 예시",
                                    value = """
                                            {
                                              "id": 1,
                                              "areaName": "집현관",
                                              "areaPolygon": {
                                                "coordinates": [
                                                  {
                                                    "lat": 37.549313,
                                                    "lng": 127.0741179
                                                  },
                                                  {
                                                    "lat": 37.5493215,
                                                    "lng": 127.0733401
                                                  },
                                                  {
                                                    "lat": 37.5484602,
                                                    "lng": 127.0732891
                                                  },
                                                  {
                                                    "lat": 37.548439,
                                                    "lng": 127.0740777
                                                  },
                                                  {
                                                    "lat": 37.549313,
                                                    "lng": 127.0741179
                                                  }
                                                ]
                                              },
                                              "marker": {
                                                "lat": 37.5488866,
                                                "lng": 127.0737063179485
                                              }
                                            }
                                            """


                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 파라미터가 유효하지 않은 경우 (누락 or 위도 경도 범위에서 벗어난 경우 등)",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "요청 파라미터로 주어진 위도 경도 값이 잘못된 범위일 경우 예시",
                                    value = """
                                            {
                                                "title": "유효하지 않은 입력값",
                                                "status": 400,
                                                "detail": "lng: 경도는 180 이하여야 합니다.",
                                                "instance": "/api/school-areas/contains"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "주어진 좌표에 해당하는 구역이 존재하지 않는 경우",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "주어진 좌표의 구역이 존재하지 않는 경우 예시",
                                    value = """
                                            {
                                                 "title": "유효하지 않은 구역",
                                                 "status": 404,
                                                 "detail": "요청하신 좌표는 세종대학교를 벗어났습니다.",
                                                 "instance": "/api/school-areas/contains"
                                            }
                                            """
                            )
                    )
            ),

    })
    ResponseEntity<SchoolAreaResponse> findArea(@ParameterObject @Valid LatLngRequest request);


    @Operation(summary = "세종대학교 구역 전체 조회",
            description = "세종대학교의 전체 구역 정보를 응답합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "세종대학교 전체 구역 정보 조회에 성공한 경우",
                    content = @Content(
                            schema = @Schema(implementation = AllSchoolAreasResponse.class),
                            examples = @ExampleObject(
                                    name = "세종대학교 전체 구역 정보 조회 성공 예시",
                                    value = """
                                            {
                                              "schoolAreas": [
                                                {
                                                  "id": 1,
                                                  "areaName": "집현관",
                                                  "areaPolygon": {
                                                    "coordinates": [
                                                      {
                                                        "lat": 37.549313,
                                                        "lng": 127.0741179
                                                      },
                                                      {
                                                        "lat": 37.5493215,
                                                        "lng": 127.0733401
                                                      }
                                                    ]
                                                  },
                                                  "marker": {
                                                    "lat": 37.5488866,
                                                    "lng": 127.0737063179485
                                                  }
                                                },
                                                {
                                                  "id": 2,
                                                  "areaName": "대양홀",
                                                  "areaPolygon": {
                                                    "coordinates": [
                                                      {
                                                        "lat": 37.551,
                                                        "lng": 127.074
                                                      },
                                                      {
                                                        "lat": 37.552,
                                                        "lng": 127.075
                                                      }
                                                    ]
                                                  },
                                                  "marker": {
                                                    "lat": 37.5515,
                                                    "lng": 127.0745
                                                  }
                                                }
                                              ],
                                              "count": 2
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<AllSchoolAreasResponse> findAll();
}
