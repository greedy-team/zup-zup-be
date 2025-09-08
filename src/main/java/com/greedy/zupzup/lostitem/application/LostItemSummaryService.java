package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.lostitem.application.dto.LostItemSummaryCommand;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LostItemSummaryService {

    private final LostItemRepository repository;

    @Transactional(readOnly = true)
    public List<LostItemSummaryCommand> getSummary(Long categoryId) {
        return repository.findAreaSummaries(categoryId, LostItemStatus.REGISTERED.name())
                .stream()
                .map(LostItemSummaryCommand::from)
                .toList();
    }
}
