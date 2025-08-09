package com.greedy.zupzup.schoolarea.presentation.dto;

import com.greedy.zupzup.schoolarea.domain.SchoolArea;

public record SchoolAreaResponse(
        Long id,
        String areaName,
        CoordinatesResponse areaPolygon,
        LatLngResponse marker
) {
    public static SchoolAreaResponse from(SchoolArea schoolArea) {
        return new SchoolAreaResponse(
                schoolArea.getId(),
                schoolArea.getAreaName(),
                CoordinatesResponse.from(schoolArea.getArea()),
                new LatLngResponse(schoolArea.getArea().getInteriorPoint().getY(), schoolArea.getArea().getInteriorPoint().getX())
        );
    }
}
