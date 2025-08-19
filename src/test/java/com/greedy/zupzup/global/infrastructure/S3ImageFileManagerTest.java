package com.greedy.zupzup.global.infrastructure;

import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.global.exception.CommonException;
import com.greedy.zupzup.global.exception.InfrastructureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.core.sync.RequestBody;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;


class S3ImageFileManagerTest extends ServiceUnitTest {

    private static final String directory = "lost-items";

    @Mock
    private S3Client s3Client;
    private S3ImageFileManager s3ImageFileManager;

    @BeforeEach
    void setUp() {
        s3ImageFileManager = new S3ImageFileManager(
                s3Client,
                "test-bucket",
                "http://test-bucket.url.com"
        );
    }

    @Test
    void 이미지_파일_업로드에_성공하면_생성된_URL을_반환해야_한다() {

        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "images", "image.png", "image/png", "test".getBytes());

        // when
        String resultUrl = s3ImageFileManager.upload(mockFile, directory);

        // then
        assertSoftly(softly -> {
            softly.assertThat(resultUrl).startsWith("http://test-bucket.url.com/lost-items/");
            softly.assertThat(resultUrl).endsWith(".png");
        });

        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

        then(s3Client).should().putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());

        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();

        assertSoftly(softly -> {
            softly.assertThat(capturedRequest.bucket()).isEqualTo("test-bucket");
            softly.assertThat(capturedRequest.key()).contains(directory);
            softly.assertThat(capturedRequest.key()).endsWith(".png");
            softly.assertThat(capturedRequest.contentType()).isEqualTo("image/png");
            softly.assertThat(capturedRequest.contentLength()).isEqualTo(mockFile.getSize());
        });
    }

    @Test
    void 이미지_파일이_null이라면_예외가_발생해야_한다() {

        // given & when & then
        assertThatThrownBy(() -> s3ImageFileManager.upload(null, directory))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(CommonException.IMAGE_NOT_PROVIDED.getDetail());

        then(s3Client).should(never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 빈_파일이_전달되면_예외가_발생해야_한다() {

        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "images", "empty.png", "image/png", new byte[0]);

        // when & then
        assertThatThrownBy(() -> s3ImageFileManager.upload(emptyFile, directory))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(CommonException.IMAGE_NOT_PROVIDED.getDetail());

        then(s3Client).should(never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 파일의_크기가_10MB를_초과하면_예외가_발생해야_한다() {

        // given
        byte[] largeData = new byte[11 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "images", "large-image.jpg", "image/jpeg", largeData);

        // when & then
        assertThatThrownBy(() -> s3ImageFileManager.upload(largeFile, directory))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(CommonException.IMAGE_SIZE_TOO_LARGE.getDetail());

        then(s3Client).should(never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 지원하지_않는_확장자_파일이_전달되면_예외가_발생해야_한다() {

        // given
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "files", "zupzup.pdf", "application/pdf", "test".getBytes());

        // when & then
        assertThatThrownBy(() -> s3ImageFileManager.upload(unsupportedFile, directory))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(CommonException.UNSUPPORTED_FILE_EXTENSION.getDetail());

        then(s3Client).should(never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void 확장자가_없는_파일이_전달되면_예외가_발생해야_한다() {

        // given
        MockMultipartFile noExtensionFile = new MockMultipartFile(
                "files", "document", "text/plain", "test".getBytes());

        // when & then
        assertThatThrownBy(() -> s3ImageFileManager.upload(noExtensionFile, directory))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining(CommonException.UNSUPPORTED_FILE_EXTENSION.getDetail());

        then(s3Client).should(never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void jpg_jpeg_png_gif_확장자를_가진_파일이_전달되면_대소문자에_상관없이_파일_업로드가_성공해야_한다() {

        // given & when
        MockMultipartFile jpgFile = new MockMultipartFile(
                "images", "test.jpg", "image/jpg", "test".getBytes());
        String jpgResult = s3ImageFileManager.upload(jpgFile, directory);

        MockMultipartFile jpegFile = new MockMultipartFile(
                "images", "test.jpeg", "image/jpeg", "test".getBytes());
        String jpegResult = s3ImageFileManager.upload(jpegFile, directory);

        MockMultipartFile gifFile = new MockMultipartFile(
                "images", "test.gif", "image/gif", "test".getBytes());
        String gifResult = s3ImageFileManager.upload(gifFile, directory);

        MockMultipartFile upperCaseFile = new MockMultipartFile(
                "images", "test.JPG", "image/jpg", "test".getBytes());
        String upperCaseResult = s3ImageFileManager.upload(upperCaseFile, directory);

        // then
        assertSoftly(softly -> {
            softly.assertThat(jpgResult).endsWith(".jpg");
            softly.assertThat(jpegResult).endsWith(".jpeg");
            softly.assertThat(gifResult).endsWith(".gif");
            softly.assertThat(upperCaseResult).endsWith(".JPG");
        });

        then(s3Client).should(times(4)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void S3_업로드가_실패하면_Infrastructure_예외가_발생해야_한다() {

        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "images", "image.png", "image/png", "test".getBytes());

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willThrow(S3Exception.builder().message("S3 access denied").build());

        // when & then
        assertThatThrownBy(() -> s3ImageFileManager.upload(mockFile, directory))
                .isInstanceOf(InfrastructureException.class)
                .hasMessageContaining(CommonException.IMAGE_UPLOAD_FAILED.getDetail());
    }

    @Test
    void S3에_저장되는_파일명은_같은_파일명을_가진_파일이_전달되어도_UUID_기반으로_고유한_파일_이름이_생성된다() {

        // given
        MockMultipartFile mockFile1 = new MockMultipartFile(
                "images", "same-name.jpg", "image/jpeg", "test".getBytes());
        MockMultipartFile mockFile2 = new MockMultipartFile(
                "images", "same-name.jpg", "image/jpeg", "test".getBytes());

        // when
        String resultUrl1 = s3ImageFileManager.upload(mockFile1, directory);
        String resultUrl2 = s3ImageFileManager.upload(mockFile2, directory);

        // then
        assertSoftly(softly -> {
            softly.assertThat(resultUrl1).isNotEqualTo(resultUrl2);
            softly.assertThat(resultUrl1).endsWith(".jpg");
            softly.assertThat(resultUrl2).endsWith(".jpg");
        });

        then(s3Client).should(times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
