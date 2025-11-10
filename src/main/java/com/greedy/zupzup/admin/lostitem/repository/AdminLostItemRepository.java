package com.greedy.zupzup.admin.lostitem.repository;

import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminLostItemRepository extends JpaRepository<LostItem, Long> {
    @Modifying
    @Query("UPDATE LostItem li SET li.status = :targetStatus WHERE li.id IN :ids AND li.status = :expectedStatus")
    int updateStatusBulkByIds(
            @Param("ids") List<Long> ids,
            @Param("targetStatus") LostItemStatus targetStatus,
            @Param("expectedStatus") LostItemStatus expectedStatus
    );

    @Modifying
    @Query("DELETE FROM LostItem li WHERE li.id IN :ids")
    int deleteBulkByIds(@Param("ids") List<Long> ids);

    @Query("""
    select li
    from LostItem li
        join fetch li.category c
        join fetch li.foundArea sa
    where li.status = :status
    order by li.createdAt desc
    """)
    List<LostItem> findPendingItems(@Param("status") LostItemStatus status, Pageable pageable);
}
