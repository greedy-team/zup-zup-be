package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public record LostItemListResponse(
        int count,
        List<LostItemViewResponse> items,
        PageInfoResponse pageInfo
) {
    public static LostItemListResponse of(Page<LostItemListCommand> page, Map<Long, String> repImageMap) {
        List<LostItemViewResponse> items = page.getContent().stream()
                .map(c -> LostItemViewResponse.from(c, repImageMap.get(c.id())))
                .toList();

        return new LostItemListResponse(
                page.getNumberOfElements(),
                items,
                PageInfoResponse.from(page)
        );
    }
}
