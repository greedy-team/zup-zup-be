package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.lostitem.LostItem;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    @Query(
            value = """
                    select
                        li.id as id,
                        c.id as categoryId,
                        c.name as categoryName,
                        c.iconUrl as categoryIconUrl,
                        sa.id as schoolAreaId,
                        sa.areaName as schoolAreaName,
                        li.foundAreaDetail as findArea,
                        li.createdAt as createdAt
                    from LostItem li
                        join li.category c
                        join li.foundArea sa
                    where li.status = com.greedy.zupzup.lostitem.LostItemStatus.REGISTERED
                      and (:categoryId is null or c.id = :categoryId)
                      and (:schoolAreaId is null or sa.id = :schoolAreaId)
                    """
            ,
            countQuery = """
                    select count(li)
                    from LostItem li
                        join li.category c
                        join li.foundArea sa
                    where li.status = com.greedy.zupzup.lostitem.LostItemStatus.REGISTERED
                      and (:categoryId is null or c.id = :categoryId)
                      and (:schoolAreaId is null or sa.id = :schoolAreaId)
                    """
    )
    Page<LostItemListProjection> findList(
            @Param("categoryId") Long categoryId,
            @Param("schoolAreaId") Long schoolAreaId,
            Pageable pageable
    );
}
