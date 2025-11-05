package com.greedy.zupzup.admin.lostitem.presentation.dto;

import com.greedy.zupzup.admin.lostitem.application.dto.AdminLostItemSimpleCommand;

import com.greedy.zupzup.lostitem.presentation.dto.PageInfoResponse;
import java.util.List;
import org.springframework.data.domain.Page;

public record AdminPendingLostItemListResponse(
        int count,
        List<AdminLostItemSimpleCommand> items,
        PageInfoResponse pageInfo
) {
    public static AdminPendingLostItemListResponse of(Page<AdminLostItemSimpleCommand> pageResult) {
        List<AdminLostItemSimpleCommand> items = pageResult.getContent();

        return new AdminPendingLostItemListResponse(
                items.size(),
                items,
                PageInfoResponse.from(pageResult)
        );
    }
}
