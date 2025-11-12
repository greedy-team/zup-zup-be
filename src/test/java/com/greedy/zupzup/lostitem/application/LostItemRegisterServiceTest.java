package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.common.ServiceUnitTest;
import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.CreateImageCommand;
import com.greedy.zupzup.lostitem.application.dto.CreateLostItemCommand;
import com.greedy.zupzup.lostitem.application.dto.LostItemRegisterData;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;


class LostItemRegisterServiceTest extends ServiceUnitTest {

    @InjectMocks
    private LostItemRegisterService lostItemRegisterService;

    @Mock
    private LostItemStorageService lostItemStorageService;

    private CreateLostItemCommand createDummyCommand() {
        List<MultipartFile> images = createDummyImages(3);
        List<CreateImageCommand> imageCommands = images.stream()
                .map(img -> new CreateImageCommand(img, 1))
                .toList();

        return new CreateLostItemCommand(
                "desc", "deposit", 1L, "detail", 1L, List.of(), imageCommands
        );
    }

    private List<MultipartFile> createDummyImages(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new MockMultipartFile("images", "image" + i + ".jpg", "image/jpeg", new byte[]{}))
                .collect(Collectors.toList());
    }

    @Test
    void 분실물_등록_절차에_성공해야_한다() {
        // given
        CreateLostItemCommand command = createDummyCommand();
        LostItemRegisterData mockValidatedData = mock(LostItemRegisterData.class);
        LostItem mockSavedLostItem = mock(LostItem.class);

        given(lostItemStorageService.getValidRegisterData(command)).willReturn(mockValidatedData);
        given(s3ImageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        given(lostItemStorageService.createNewLostItem(eq(command), eq(mockValidatedData), anyList())).willReturn(mockSavedLostItem);

        // when
        LostItem result = lostItemRegisterService.registLostItem(command);

        // then
        assertThat(result).isEqualTo(mockSavedLostItem);
        // 호출 순서 검증
        then(lostItemStorageService).should(times(1)).getValidRegisterData(command);
        then(s3ImageFileManager).should(times(3)).upload(any(MultipartFile.class), any(String.class));
        then(lostItemStorageService).should(times(1)).createNewLostItem(eq(command), eq(mockValidatedData), anyList());

        // 롤백 로직 호출 x
        then(s3FileCleanupService).should(never()).cleanupOrphanFiles(anyList());
    }

    @Test
    void 유효성_검사_실패_시_S3업로드와_DB저장을_호출하지_않아야_한다() {
        // given
        CreateLostItemCommand command = createDummyCommand();
        ApplicationException validationException = new ApplicationException(CategoryException.CATEGORY_NOT_FOUND);

        given(lostItemStorageService.getValidRegisterData(command)).willThrow(validationException);

        // when & then
        assertThatThrownBy(() -> lostItemRegisterService.registLostItem(command))
                .isEqualTo(validationException);

        // S3 업로드, DB 저장 호출 x
        then(s3ImageFileManager).should(never()).upload(any(MultipartFile.class), any(String.class));
        then(lostItemStorageService).should(never()).createNewLostItem(any(), any(), anyList());
        then(s3FileCleanupService).should(never()).cleanupOrphanFiles(anyList());
    }

    @Test
    void S3_업로드_실패_시_DB저장을_호출하지_않아야_한다() {
        // given
        CreateLostItemCommand command = createDummyCommand();
        LostItemRegisterData mockValidatedData = mock(LostItemRegisterData.class);
        RuntimeException uploadException = new RuntimeException("S3 Error");

        given(lostItemStorageService.getValidRegisterData(command)).willReturn(mockValidatedData);
        given(s3ImageFileManager.upload(any(MultipartFile.class), any(String.class))).willThrow(uploadException);

        // when & then
        assertThatThrownBy(() -> lostItemRegisterService.registLostItem(command))
                .isEqualTo(uploadException);

        // DB 저장 롤백 로직 호출 x
        then(lostItemStorageService).should(never()).createNewLostItem(any(), any(), anyList());
        then(s3FileCleanupService).should(never()).cleanupOrphanFiles(anyList());
    }

    @Test
    @DisplayName("실패: 3.DB 저장 실패 시 S3 롤백 호출")
    void DB_저장_실패_시_S3_롤백을_호출하고_예외를_던져야_한다() {
        // given
        CreateLostItemCommand command = createDummyCommand();
        LostItemRegisterData mockValidatedData = mock(LostItemRegisterData.class);
        ApplicationException dbException = new ApplicationException(LostItemException.REGISTRATION_FAILED);

        // 검증 성공
        given(lostItemStorageService.getValidRegisterData(command)).willReturn(mockValidatedData);
        // S3 업로드 성공
        given(s3ImageFileManager.upload(any(MultipartFile.class), any(String.class))).willReturn("http://image.url/test.jpg");
        // DB 저장 실패
        given(lostItemStorageService.createNewLostItem(eq(command), eq(mockValidatedData), anyList())).willThrow(dbException);

        // when & then
        assertThatThrownBy(() -> lostItemRegisterService.registLostItem(command))
                .isInstanceOf(ApplicationException.class)
                .hasMessage(LostItemException.REGISTRATION_FAILED.getDetail());

        // 롤백 로직 호출
        then(s3FileCleanupService).should(times(1)).cleanupOrphanFiles(anyList());
    }

}
