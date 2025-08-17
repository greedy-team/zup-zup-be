package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    @Query(
            value = """
                    select
                        li.id              as id,
                        c.id               as categoryId,
                        c.name             as categoryName,
                        c.iconUrl          as categoryIconUrl,
                        sa.id              as schoolAreaId,
                        sa.areaName        as schoolAreaName,
                        li.foundAreaDetail as foundAreaDetail,
                        li.createdAt       as createdAt
                    from LostItem li
                        join li.category  c
                        join li.foundArea sa
                    where li.status = :status
                      and (:categoryId   is null or c.id  = :categoryId)
                      and (:schoolAreaId is null or sa.id = :schoolAreaId)
                    order by li.createdAt desc
                    """,
            countQuery = """
                    select count(li)
                    from LostItem li
                        join li.category  c
                        join li.foundArea sa
                    where li.status = :status
                      and (:categoryId   is null or c.id  = :categoryId)
                      and (:schoolAreaId is null or sa.id = :schoolAreaId)
                    """
    )
    Page<LostItemListProjection> findList(
            @Param("categoryId") Long categoryId,
            @Param("schoolAreaId") Long schoolAreaId,
            @Param("status") LostItemStatus status,
            Pageable pageable
    );

    @Query("""
            select li from LostItem li
            join fetch li.category
            where li.id = :lostItemId
            """)
    Optional<LostItem> findWithCategoryById(Long lostItemId);

    default LostItem getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));
    }

    default LostItem getWithCategoryById(Long id) {
        return findWithCategoryById(id)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));
    }

    @Query("""
        select li
          from LostItem li
          join fetch li.category c
          join fetch li.foundArea sa
         where li.id = :id
    """)
    Optional<LostItem> findWithCategoryAndAreaById(@Param("id") Long id);

    default LostItem getWithCategoryAndAreaById(Long id) {
        return findWithCategoryAndAreaById(id)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));
    }
}
