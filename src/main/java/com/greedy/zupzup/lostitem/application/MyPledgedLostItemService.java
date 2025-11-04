package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.lostitem.application.dto.MyPledgedLostItemCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.MyPledgedListResponse;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPledgedLostItemService {

    private final LostItemRepository lostItemRepository;

    public LostItemListResponse getMyPledgedLostItems(Long memberId, int page, int limit) {
        Page<MyPledgedLostItemCommand> pledgedItems =
                lostItemRepository.findPledgedLostItemsByMemberId(memberId, PageRequest.of(page - 1, limit))
                        .map(MyPledgedLostItemCommand::from);

        return LostItemListResponse.of(pledgedItems.map(MyPledgedListResponse::from));
    }
}
