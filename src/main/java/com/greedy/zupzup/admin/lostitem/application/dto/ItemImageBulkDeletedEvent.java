package com.greedy.zupzup.admin.lostitem.application.dto;

import java.util.List;

public record ItemImageBulkDeletedEvent(
        List<String> imageUrls,
        int count
) {
    public static ItemImageBulkDeletedEvent from(List<String> imageUrls) {
        return new ItemImageBulkDeletedEvent(imageUrls, imageUrls.size());
    }
}
