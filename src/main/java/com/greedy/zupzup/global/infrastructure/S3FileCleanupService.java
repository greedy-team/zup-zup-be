package com.greedy.zupzup.global.infrastructure;

import com.greedy.zupzup.admin.lostitem.application.dto.ItemImageBulkDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3FileCleanupService {

    private final S3ImageFileManager s3ImageFileManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLostItemBulkDelete(ItemImageBulkDeletedEvent event) {
        try {
            event.imageUrls().forEach(s3ImageFileManager::delete);
            log.info("S3: 이미지 파일 {}개 삭제 완료.", event.count());
        } catch (Exception e) {
            log.error("S3: 비동기 이미지 삭제 중 오류 발생.", e);
        }
    }

    @Async
    public void cleanupOrphanFiles(List<String> imageUrls) {
        try {
            imageUrls.forEach(s3ImageFileManager::delete);
            log.info("S3: 이미지 파일 {}개 삭제(롤백) 완료.", imageUrls.size());
        } catch (Exception e) {
            log.error("S3: 비동기 이미지 삭제 중 오류 발생.", e);
        }
    }

}
