package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemSummaryCommand;
import java.util.List;

public record LostItemSummaryResponse(List<AreaSummary> areas) {

    public record AreaSummary(
            Long schoolAreaId,
            String schoolAreaName,
            Long lostCount) {
    }

    public static LostItemSummaryResponse of(List<LostItemSummaryCommand> list) {
        List<AreaSummary> areas = list.stream()
                .map(command -> new AreaSummary(command.schoolAreaId(), command.schoolAreaName(), command.lostCount()))
                .toList();
        return new LostItemSummaryResponse(areas);
    }
}
