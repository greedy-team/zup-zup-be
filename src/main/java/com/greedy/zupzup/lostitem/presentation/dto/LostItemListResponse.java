package com.greedy.zupzup.lostitem.presentation.dto;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import java.util.List;
import org.springframework.data.domain.Page;

public record LostItemListResponse(
        int count,
        List<LostItemResponse> items,
        PageInfoResponse pageInfo
) {
    public static LostItemListResponse of(Page<LostItemListCommand> page) {
        List<LostItemResponse> items = page.getContent().stream()
                .map(LostItemResponse::from)
                .toList();

        PageInfoResponse pageInfo = PageInfoResponse.of(
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasPrevious(),
                page.hasNext()
        );

        return new LostItemListResponse(page.getNumberOfElements(), items, pageInfo);
    }
}
