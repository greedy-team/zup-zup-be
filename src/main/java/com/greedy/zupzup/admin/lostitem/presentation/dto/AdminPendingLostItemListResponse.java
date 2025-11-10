package com.greedy.zupzup.admin.lostitem.presentation.dto;

import com.greedy.zupzup.admin.lostitem.application.dto.AdminLostItemResult;

import com.greedy.zupzup.lostitem.presentation.dto.PageInfoResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public record AdminPendingLostItemListResponse(
        int count,
        List<AdminLostItemResult> items,
        PageInfoResponse pageInfo
) {
    public static AdminPendingLostItemListResponse of(Page<AdminLostItemResult> pageResult) {
        List<AdminLostItemResult> items = pageResult.getContent();

        return new AdminPendingLostItemListResponse(
                items.size(),
                items,
                PageInfoResponse.from(pageResult)
        );
    }

    public static AdminPendingLostItemListResponse of(
            List<AdminLostItemResult> commands,
            int page,
            int limit,
            long totalCount
    ) {
        PageInfoResponse pageInfo = new PageInfoResponse(
                page,
                limit,
                totalCount,
                (int) Math.ceil((double) totalCount / limit),
                page > 1,
                ((long) page * limit) < totalCount
        );

        return new AdminPendingLostItemListResponse(
                commands.size(),
                commands,
                pageInfo
        );
    }

}
