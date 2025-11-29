package com.greedy.zupzup.pledge.repository;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import com.greedy.zupzup.pledge.domain.Pledge;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PledgeRepository extends JpaRepository<Pledge, Long> {

    boolean existsByLostItem_IdAndOwner_Id(Long lostItemId, Long ownerId);

    default Pledge getByLostItemId(Long lostItemId) {
        return findByLostItemId(lostItemId)
                .orElseThrow(() -> new ApplicationException(LostItemException.PLEDGE_NOT_FOUND));
    }

    Optional<Pledge> findByLostItemId(Long lostItemId);

}
