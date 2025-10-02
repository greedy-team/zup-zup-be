package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import java.util.stream.Collectors;

public record LostItemListResponse(
        long count,
        List<? extends LostItemViewItem> items,
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

    public static LostItemListResponse of(Page<MyPledgedLostItemViewResponse> page) {
        PageInfoResponse pageInfo = PageInfoResponse.from(page);

        List<? extends LostItemViewItem> items = page.getContent().stream()
                .map(item -> (LostItemViewItem) item)
                .collect(Collectors.toList());

        return new LostItemListResponse(
                page.getNumberOfElements(),
                items,
                pageInfo
        );
    }
}
