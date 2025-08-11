package com.greedy.zupzup.schoolarea.presentation.dto;

import com.greedy.zupzup.schoolarea.domain.SchoolArea;

import java.util.List;

public record AllSchoolAreasResponse(
        List<SchoolAreaResponse> schoolAreas,
        int count
) {
    public static AllSchoolAreasResponse of(List<SchoolArea> schoolAreas) {
        return new AllSchoolAreasResponse(
                schoolAreas.stream()
                        .map(SchoolAreaResponse::from)
                        .toList()
                ,
                schoolAreas.size()
        );
    }
}
