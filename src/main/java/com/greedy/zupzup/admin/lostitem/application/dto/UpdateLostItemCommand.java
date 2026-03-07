package com.greedy.zupzup.admin.lostitem.application.dto;

import com.greedy.zupzup.admin.lostitem.presentation.dto.UpdateLostItemRequest;
import com.greedy.zupzup.lostitem.presentation.dto.ItemFeatureRequest;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record UpdateLostItemCommand(
        Long lostItemId,
        String description,
        String depositArea,
        Long foundAreaId,
        String foundAreaDetail,
        Long categoryId,
        List<ItemFeatureRequest> featureOptions,
        List<Long> keepImageIds,
        List<MultipartFile> newImages
) {
    public static UpdateLostItemCommand of(Long lostItemId, UpdateLostItemRequest request,
                                           List<MultipartFile> newImages) {
        return new UpdateLostItemCommand(
                lostItemId,
                request.description(),
                request.depositArea(),
                request.foundAreaId(),
                request.foundAreaDetail(),
                request.categoryId(),
                request.featureOptions(),
                request.keepImageIds() == null ? List.of() : request.keepImageIds(),
                newImages == null ? List.of() : newImages
        );
    }
}
