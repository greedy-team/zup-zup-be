package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.repository.LostItemSummaryRepository;
import java.util.List;

public record LostItemSummaryCommand(
        Long schoolAreaId,
        String schoolAreaName,
        Long lostCount
) {
    public static LostItemSummaryCommand from(LostItemSummaryRepository.LostItemSummaryProjection projection) {
        return new LostItemSummaryCommand(
                projection.getSchoolAreaId(),
                projection.getSchoolAreaName(),
                projection.getLostCount()
        );
    }

    public record Response(List<LostItemSummaryCommand> areas) {}
}
