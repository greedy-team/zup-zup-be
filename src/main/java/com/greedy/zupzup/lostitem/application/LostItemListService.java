package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.repository.LostItemListProjection;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LostItemListService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 50;

    private final LostItemRepository lostItemRepository;

    @Transactional(readOnly = true)
    public Page<LostItemListCommand> getLostItems(Long categoryId, Long schoolAreaId, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int rawLimit = limit == null ? DEFAULT_LIMIT : limit;
        int safeLimit = Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, rawLimit));

        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<LostItemListProjection> slice = lostItemRepository.findList(categoryId, schoolAreaId, pageable);

        return slice.map(LostItemListCommand::from);
    }
}
