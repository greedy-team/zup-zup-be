package com.greedy.zupzup.pledge.repository;

import com.greedy.zupzup.pledge.domain.Pledge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PledgeRepository extends JpaRepository<Pledge, Long> {

    boolean existsByLostItem_IdAndOwner_Id(Long lostItemId, Long ownerId);
}
