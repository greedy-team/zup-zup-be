package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.lostitem.application.dto.LostItemListCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.pledge.application.PledgeQueryService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPledgedLostItemService {

    private final PledgeQueryService pledgeQueryService;
    private final LostItemViewService lostItemViewService;

    public LostItemListResponse getMyPledgedLostItems(Long memberId, int page, int limit) {
        Page<Long> pledgedIds = pledgeQueryService.getPledgedLostItemIds(memberId, page, limit);

        Page<LostItemListCommand> pageResult =
                lostItemViewService.getLostItemsByIds(pledgedIds.getContent(), page, limit);

        List<Long> ids = pageResult.getContent().stream().map(LostItemListCommand::id).toList();
        Map<Long, String> repImageMap = lostItemViewService.getRepresentativeImageMapByItemIds(ids);

        return LostItemListResponse.of(pageResult, repImageMap);
    }
}
