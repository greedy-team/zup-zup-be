package com.greedy.zupzup.lostitem.presentation.dto;

import java.util.List;

public record LostItemListResponse(
        int count,
        List<LostItemResponse> items,
        PageInfoResponse pageInfo
) { }
