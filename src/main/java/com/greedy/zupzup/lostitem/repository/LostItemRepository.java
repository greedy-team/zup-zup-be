package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.application.dto.LostItemDetailViewCommand;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {

    interface LostItemSummaryProjection {
        Long getSchoolAreaId();

        String getSchoolAreaName();

        Long getLostCount();
    }

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

    @Query("""
            select li from LostItem li
            join fetch li.category
            join fetch li.foundArea
            where li.id = :lostItemId
            """)
    Optional<LostItem> findWithCategoryAndAreaById(@Param("lostItemId") Long lostItemId);

    @Query(value = """
            select 
                sa.id as schoolAreaId,
                sa.area_name as schoolAreaName,
                COALESCE(t.cnt, 0) as lostCount
            from
                school_area sa
                left join (
                        select found_area_id, COUNT(*) as cnt
                        from lost_item
                        where status = :status
                          and (:categoryId is NULL or category_id = :categoryId)
                        GROUP BY found_area_id
                    ) t on t.found_area_id = sa.id
            """,
            nativeQuery = true)
    List<LostItemSummaryProjection> findAreaSummaries(
            @Param("categoryId") Long categoryId,
            @Param("status") String status
    );

    default LostItem getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));
    }

    default LostItem getWithCategoryById(Long id) {
        return findWithCategoryById(id)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));
    }

    default LostItem getWithCategoryAndAreaById(Long id) {
        return findWithCategoryAndAreaById(id)
                .orElseThrow(() -> new ApplicationException(LostItemException.LOST_ITEM_NOT_FOUND));
    }

    @Query("""
                select
                    li.id                as id,
                    c.id                 as categoryId,
                    c.name               as categoryName,
                sa.id                as schoolAreaId,
                    sa.areaName          as schoolAreaName,
                    li.foundAreaDetail   as foundAreaDetail,
                    li.createdAt         as createdAt,
                    img.imageKey         as representativeImageUrl,
                    p.createdAt          as pledgedAt,
                    li.depositArea       as depositArea
                from Pledge p
                join p.lostItem li
                join li.category c
                join li.foundArea sa
                left join li.images img on img.imageOrder = 0
                where p.owner.id = :memberId
                order by p.createdAt desc
            """)
    Page<MyPledgedLostItemProjection> findPledgedLostItemsByMemberId(
            @Param("memberId") Long memberId,
            Pageable pageable
    );

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
