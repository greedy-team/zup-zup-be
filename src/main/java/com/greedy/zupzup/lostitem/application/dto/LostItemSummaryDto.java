package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.lostitem.repository.LostItemSummaryRepository;
import java.util.List;

public record LostItemSummaryDto(
        Long schoolAreaId,
        String schoolAreaName,
        Long lostCount
) {
    public static LostItemSummaryDto from(LostItemSummaryRepository.LostItemSummaryProjection p) {
        return new LostItemSummaryDto(
                p.getSchoolAreaId(),
                p.getSchoolAreaName(),
                p.getLostCount()
        );
    }

    public record Response(List<LostItemSummaryDto> areas) {}
}
