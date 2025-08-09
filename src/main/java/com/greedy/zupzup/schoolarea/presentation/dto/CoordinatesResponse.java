package com.greedy.zupzup.schoolarea.presentation.dto;

import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.stream.Stream;

public record CoordinatesResponse(
        List<LatLngResponse> coordinates
) {
    public static CoordinatesResponse from(Polygon polygon) {
        return new CoordinatesResponse(convertPolygonToCoordinates(polygon));
    }

    // jts 의 폴리곤은 lng 경도 lat 위도 순
    private static List<LatLngResponse> convertPolygonToCoordinates(Polygon polygon) {
        return Stream.of(polygon.getExteriorRing().getCoordinates())
                .map(coordinate -> new LatLngResponse(coordinate.y, coordinate.x))
                .toList();
    }
}
