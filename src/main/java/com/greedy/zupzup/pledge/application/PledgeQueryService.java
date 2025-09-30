package com.greedy.zupzup.pledge.application;

import com.greedy.zupzup.pledge.repository.PledgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PledgeQueryService {

    private final PledgeRepository pledgeRepository;

    public Page<Long> getPledgedLostItemIds(Long memberId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        return pledgeRepository.findByOwner_IdOrderByCreatedAtDesc(memberId, pageable)
                .map(p -> p.getLostItem().getId());
    }
}
