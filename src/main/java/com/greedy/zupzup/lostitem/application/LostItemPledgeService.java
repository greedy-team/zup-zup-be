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
        Pledge pledge = validateAndGetPledge(memberId, lostItemId, LostItemException.INVALID_STATUS_FOR_PLEDGE_CANCEL);

        LostItem lostItem = pledge.getLostItem();
        lostItem.cancelPledge();
        pledgeRepository.delete(pledge);

        return CancelPledgeResponse.of(lostItemId, lostItem.getStatus().name());
    }

    @Transactional
    public FoundCompleteResponse completeFound(Long memberId, Long lostItemId) {
        Pledge pledge = validateAndGetPledge(memberId, lostItemId,
                LostItemException.INVALID_STATUS_FOR_PLEDGE_COMPLETE);

        LostItem lostItem = pledge.getLostItem();
        lostItem.completeFound();
        pledgeRepository.delete(pledge);

        return FoundCompleteResponse.of(lostItemId, lostItem.getStatus().name());
    }

    private Pledge validateAndGetPledge(Long memberId, Long lostItemId, LostItemException statusException) {
        LostItem lostItem = lostItemRepository.getById(lostItemId);

        if (lostItem.getStatus() != LostItemStatus.PLEDGED) {
            throw new ApplicationException(statusException);
        }

        Pledge pledge = pledgeRepository.getByLostItemId(lostItemId);

        pledge.validateOwner(memberId);

        return pledge;
    }
}

