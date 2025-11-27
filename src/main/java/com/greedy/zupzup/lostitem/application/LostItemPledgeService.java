package com.greedy.zupzup.lostitem.application;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.lostitem.presentation.dto.CancelPledgeResponse;
import com.greedy.zupzup.lostitem.presentation.dto.FoundCompleteResponse;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.pledge.domain.Pledge;
import com.greedy.zupzup.pledge.repository.PledgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LostItemPledgeService {

    private final LostItemRepository lostItemRepository;
    private final PledgeRepository pledgeRepository;

    @Transactional
    public CancelPledgeResponse cancelPledge(Long memberId, Long lostItemId) {

        LostItem lostItem = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));

        if (lostItem.getStatus() != LostItemStatus.PLEDGED) {
            throw new ApplicationException(LostItemException.INVALID_STATUS_FOR_PLEDGE_CANCEL);
        }

        Pledge pledge = pledgeRepository.findByLostItemId(lostItemId)
                .orElseThrow(() -> new ApplicationException(LostItemException.PLEDGE_NOT_FOUND));

        if (!pledge.getOwner().getId().equals(memberId)) {
            throw new ApplicationException(LostItemException.PLEDGE_NOT_BY_THIS_USER);
        }

        pledgeRepository.delete(pledge);

        lostItem.changeStatus(LostItemStatus.REGISTERED);

        return CancelPledgeResponse.builder()
                .lostItemId(lostItemId)
                .status("REGISTERED")
                .message("서약이 정상적으로 취소되었습니다.")
                .build();
    }

    @Transactional
    public FoundCompleteResponse completeFound(Long memberId, Long lostItemId) {

        LostItem lostItem = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));

        if (lostItem.getStatus() != LostItemStatus.PLEDGED) {
            throw new ApplicationException(LostItemException.INVALID_STATUS_FOR_PLEDGE_COMPLETE);
        }

        Pledge pledge = pledgeRepository.findByLostItemId(lostItemId)
                .orElseThrow(() -> new ApplicationException(LostItemException.PLEDGE_NOT_FOUND));

        if (!pledge.getOwner().getId().equals(memberId)) {
            throw new ApplicationException(LostItemException.PLEDGE_NOT_BY_THIS_USER);
        }

        lostItem.changeStatus(LostItemStatus.FOUND);

        pledgeRepository.delete(pledge);

        return FoundCompleteResponse.builder()
                .lostItemId(lostItemId)
                .status("FOUND")
                .message("습득 완료되었습니다.")
                .build();
    }
}

