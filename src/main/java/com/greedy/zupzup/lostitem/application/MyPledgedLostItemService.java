package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.lostitem.application.dto.MyPledgedLostItemCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemListResponse;
import com.greedy.zupzup.lostitem.presentation.dto.MyPledgedLostItemViewResponse;
import com.greedy.zupzup.pledge.application.PledgeQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

        Page<MyPledgedLostItemCommand> pageResult =
                lostItemViewService.getPledgedLostItems(pledgedIds.getContent(), page, limit);

        if (pageResult.isEmpty() && pledgedIds.getTotalElements() > 0) {
            pageResult = new PageImpl<>(
                    List.of(),
                    PageRequest.of(pageResult.getNumber(), pageResult.getSize()),
                    pledgedIds.getTotalElements()
            );
        }

        Page<MyPledgedLostItemViewResponse> viewResponsePage = pageResult.map(MyPledgedLostItemViewResponse::from);

        return LostItemListResponse.of(viewResponsePage);
    }
}
