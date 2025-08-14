package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.repository.LostItemListProjection;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LostItemListService {

    private final LostItemRepository lostItemRepository;

    @Transactional(readOnly = true)
    public Page<LostItemListCommand> getLostItems(Long categoryId, Long schoolAreaId, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<LostItemListProjection> slice = lostItemRepository.findList(categoryId, schoolAreaId, pageable);
        return slice.map(LostItemListCommand::from);
    }
}
