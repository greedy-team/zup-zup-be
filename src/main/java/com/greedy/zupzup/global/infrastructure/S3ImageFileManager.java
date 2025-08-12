package com.greedy.zupzup.global.infrastructure;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.exception.CommonException;
import com.greedy.zupzup.global.exception.InfrastructureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class S3ImageFileManager {

    private static final String PATH_DELIMITER = "/";
    private static final String FILE_EXTENSION_DELIMITER = ".";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;     // 최대 이미지 크기 10MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");

    private final AmazonS3 s3Client;
    private final String bucketName;

    public S3ImageFileManager(AmazonS3 s3Client,
                              @Value("${cloud.aws.s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    public String upload(MultipartFile multipartFile, String directory) {

        validateFile(multipartFile);

        String originalFilename = multipartFile.getOriginalFilename();
        String s3ObjectKey = directory + PATH_DELIMITER + generateUUIDFileName(originalFilename);

        ObjectMetadata objectMetadata = getObjectMetadata(multipartFile);

        try (InputStream inputStream = multipartFile.getInputStream()) {

            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, s3ObjectKey, inputStream, objectMetadata);
            s3Client.putObject(putObjectRequest);

            return s3Client.getUrl(bucketName, s3ObjectKey).toString();
        } catch (IOException e) {
            throw new InfrastructureException(CommonException.IMAGE_UPLOAD_FAILED);
        }
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

    private ObjectMetadata getObjectMetadata(MultipartFile multipartFile) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());
        return objectMetadata;
    }
}
