package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.lostitem.repository.LostItemSummaryRepository.LostItemSummaryProjection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LostItemSummaryRepository extends Repository<LostItemSummaryProjection, Long> {

    interface LostItemSummaryProjection {
        Long getSchoolAreaId();
        String getSchoolAreaName();
        Long getLostCount();
    }

    @Query(
            value = """
            select
                sa.id         as schoolAreaId,
                sa.areaName   as schoolAreaName,
                coalesce(count(li.id), 0) as lostCount
            from SchoolArea sa
                left join LostItem li
                    on li.foundArea.id = sa.id
                   and li.status = com.greedy.zupzup.lostitem.domain.LostItemStatus.REGISTERED
                   and (:categoryId is null or li.category.id = :categoryId)
            group by sa.id, sa.areaName
            """
    )
    List<LostItemSummaryProjection> findAreaSummaries(@Param("categoryId") Long categoryId);
}
