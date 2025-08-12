package com.greedy.zupzup.lostitem.application.dto;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.exception.LostItemImageException;
import com.greedy.zupzup.lostitem.presentation.dto.ItemFeatureRequest;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemRegisterRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record CreateLostItemCommand(
        String description,
        String depositArea,
        Long foundAreaId,
        String foundAreaDetail,
        Long categoryId,
        List<ItemFeatureOptionCommand> featureOptions,
        List<CreateImageCommand> images
) {
    public static CreateLostItemCommand of(LostItemRegisterRequest request, List<MultipartFile> images) {
        return new CreateLostItemCommand(
                request.description(),
                request.depositArea(),
                request.foundAreaId(),
                request.foundAreaDetail(),
                request.categoryId(),
                toItemFeatureOptionList(request.featureOptions()),
                toCreateImageCommandListWithValidation(images)
        );
    }

    public static List<ItemFeatureOptionCommand> toItemFeatureOptionList(List<ItemFeatureRequest> featureRequests) {
        return featureRequests.stream()
                .map(ItemFeatureOptionCommand::from)
                .toList();
    }

    public static List<CreateImageCommand> toCreateImageCommandListWithValidation(List<MultipartFile> images) {

        if (images == null || images.isEmpty() || images.size() > 3) {
            throw new ApplicationException(LostItemImageException.INVALID_IMAGE_COUNT);
        }
        // 이미지 순서는 0부터
        return IntStream.range(0, images.size())
                .mapToObj(i -> new CreateImageCommand(images.get(i), i))
                .collect(Collectors.toList());
    }
}
