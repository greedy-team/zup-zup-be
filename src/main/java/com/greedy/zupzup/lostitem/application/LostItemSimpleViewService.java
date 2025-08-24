package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.LostItemSimpleViewCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LostItemSimpleViewService {

    private final LostItemRepository lostItemRepository;
    private final LostItemListService lostItemListService;

    public LostItemSimpleViewCommand getBasic(Long lostItemId) {
        LostItem item = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));

        String representative =
                lostItemListService.getRepresentativeImageMapByItemIds(List.of(lostItemId))
                        .get(lostItemId);

        return LostItemSimpleViewCommand.of(item, representative);
    }
}
