package com.greedy.zupzup.global.infrastructure;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.exception.CommonException;
import com.greedy.zupzup.global.exception.InfrastructureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class S3ImageFileManager {

    private static final String PATH_DELIMITER = "/";
    private static final String FILE_EXTENSION_DELIMITER = ".";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;     // 최대 이미지 크기 10MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "heic", "webp");

    private final S3Client s3Client;
    private final String bucketName;
    private final String imageURLPrefix;

    public S3ImageFileManager(S3Client s3Client,
                              @Value("${cloud.aws.s3.bucket}") String bucketName,
                              @Value("${cloud.aws.s3.url.prefix}") String imageURLPrefix) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.imageURLPrefix = imageURLPrefix;
    }

    public String upload(MultipartFile multipartFile, String directory) {

        validateFile(multipartFile);

        String originalFilename = multipartFile.getOriginalFilename();
        String s3ObjectKey = directory + PATH_DELIMITER + generateUUIDFileName(originalFilename);

        try (InputStream inputStream = multipartFile.getInputStream()) {

            PutObjectRequest putObjectRequest = buildPutObjectRequest(multipartFile, s3ObjectKey);
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, multipartFile.getSize()));

        } catch (IOException | S3Exception e) {
            throw new InfrastructureException(CommonException.IMAGE_UPLOAD_FAILED);
        }

        return imageURLPrefix + "/" + s3ObjectKey;
    }

    public void delete(String imageKey) {
        try {
            String s3ObjectKey = extractS3ObjectKey(imageKey);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3ObjectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (S3Exception e) {
            log.error("S3 delete failed for key: {} | code: {} | message: {}",
                    imageKey,
                    e.awsErrorDetails().errorCode(),
                    e.getMessage(),
                    e);
            if (!e.awsErrorDetails().errorCode().equals("NoSuchKey")) {
                throw new InfrastructureException(CommonException.IMAGE_DELETE_FAILED);
            }
        }
    }

    private String extractS3ObjectKey(String imageURL) {
        if (imageURL.startsWith(imageURLPrefix)) {
            return imageURL.substring(imageURLPrefix.length() + 1);
        }
        return imageURL;
    }

    private PutObjectRequest buildPutObjectRequest(MultipartFile multipartFile, String s3ObjectKey) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3ObjectKey)
                .contentType(multipartFile.getContentType())
                .contentLength(multipartFile.getSize())
                .build();
    }

    private void validateFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ApplicationException(CommonException.IMAGE_NOT_PROVIDED);
        }

        if (multipartFile.getSize() > MAX_FILE_SIZE) {
            throw new ApplicationException(CommonException.IMAGE_SIZE_TOO_LARGE);
        }
    }

    private String generateUUIDFileName(String originalFileName) {
        String extension = StringUtils.getFilenameExtension(originalFileName);
        validateExtension(extension);
        return UUID.randomUUID() + FILE_EXTENSION_DELIMITER + extension;
    }

    private void validateExtension(String extension) {
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ApplicationException(CommonException.UNSUPPORTED_FILE_EXTENSION);
        }
    }

}
