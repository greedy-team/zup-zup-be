package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import java.util.List;

public record LostItemSummaryCommand(
        Long schoolAreaId,
        String schoolAreaName,
        Long lostCount
) {
    public static LostItemSummaryCommand from(LostItemRepository.LostItemSummaryProjection projection) {
        return new LostItemSummaryCommand(
                projection.getSchoolAreaId(),
                projection.getSchoolAreaName(),
                projection.getLostCount()
        );
    }

    public record Response(List<LostItemSummaryCommand> areas) {}
}
