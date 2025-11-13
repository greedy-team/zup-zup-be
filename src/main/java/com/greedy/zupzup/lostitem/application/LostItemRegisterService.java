package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.infrastructure.S3FileCleanupService;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.application.dto.*;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LostItemRegisterService {

    private static final String IMAGE_DIRECTORY = "lost-item-images";
    private final S3ImageFileManager s3ImageFileManager;
    private final LostItemStorageService lostItemStorageService;
    private final S3FileCleanupService s3FileCleanupService;

    public LostItem registLostItem(CreateLostItemCommand command) {
        LostItemRegisterData validRegisterData = lostItemStorageService.getValidRegisterData(command);
        List<UploadedImageData> uploadedImages = uploadImagesToS3(command.images());

        try {
            return lostItemStorageService.createNewLostItem(command, validRegisterData, uploadedImages);
        } catch (Exception e) {
            List<String> imageUrls = uploadedImages.stream()
                    .map(UploadedImageData::url)
                    .toList();
            s3FileCleanupService.cleanupOrphanFiles(imageUrls);
            log.error("분실물 DB 저장 실패. S3 롤백", e);
            throw new ApplicationException(LostItemException.REGISTRATION_FAILED);
        }
    }

    private List<UploadedImageData> uploadImagesToS3(List<CreateImageCommand> images) {
        return images.stream()
                .map(image -> {
                    String imageURL = s3ImageFileManager.upload(image.imageFile(), IMAGE_DIRECTORY);
                    return new UploadedImageData(imageURL, image.order());
                }).toList();
    }
}
