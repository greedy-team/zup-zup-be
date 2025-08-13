package com.greedy.zupzup.lostitem.presentation.dto;

import java.util.List;

public record LostItemSummaryResponse(List<AreaSummary> areas) {

    public record AreaSummary(
            Long schoolAreaId,
            String schoolAreaName,
            Long lostCount
    ) {
    }
}
