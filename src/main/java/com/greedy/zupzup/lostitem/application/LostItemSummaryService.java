package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.lostitem.application.dto.LostItemSummaryCommand;
import com.greedy.zupzup.lostitem.repository.LostItemSummaryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LostItemSummaryService {

    private final LostItemSummaryRepository repository;

    @Transactional(readOnly = true)
    public List<LostItemSummaryCommand> getSummary(Long categoryId) {
        return repository.findAreaSummaries(categoryId)
                .stream()
                .map(LostItemSummaryCommand::from)
                .toList();
    }
}
