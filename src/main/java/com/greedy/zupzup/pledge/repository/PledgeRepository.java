package com.greedy.zupzup.pledge.repository;

import com.greedy.zupzup.pledge.domain.Pledge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PledgeRepository extends JpaRepository<Pledge, Long> {

    boolean existsByLostItem_IdAndOwner_Id(Long lostItemId, Long ownerId);

    @Query("""
        SELECT p.lostItem.id
        FROM Pledge p
        WHERE p.owner.id = :ownerId
        ORDER BY p.createdAt DESC
    """)
    Page<Long> findLostItemIdsByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

}
