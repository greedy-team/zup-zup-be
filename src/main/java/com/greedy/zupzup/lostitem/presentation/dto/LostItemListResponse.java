package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.FoundItemListResult;
import com.greedy.zupzup.lostitem.application.dto.LostItemListResult;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

public record LostItemListResponse(
        long count,
        List<?> items,
        PageInfoResponse pageInfo
) {
    public static LostItemListResponse of(Page<LostItemListResult> page, Map<Long, String> repImageMap) {
        List<LostItemResponse> items = page.getContent().stream()
                .map(c -> LostItemResponse.from(c, repImageMap.get(c.id())))
                .toList();

        return new LostItemListResponse(
                page.getNumberOfElements(),
                items,
                PageInfoResponse.from(page)
        );
    }

    public static LostItemListResponse of(Page<MyPledgedListResponse> page) {
        PageInfoResponse pageInfo = PageInfoResponse.from(page);
        List<MyPledgedListResponse> items = page.getContent();

        return new LostItemListResponse(
                page.getNumberOfElements(),
                items,
                pageInfo
        );
    }

    public static LostItemListResponse from(Page<FoundItemListResult> page) {
        PageInfoResponse pageInfo = PageInfoResponse.from(page);
        List<FoundItemListResult> items = page.getContent();

        return new LostItemListResponse(
                page.getNumberOfElements(),
                items,
                pageInfo
        );
    }
}
