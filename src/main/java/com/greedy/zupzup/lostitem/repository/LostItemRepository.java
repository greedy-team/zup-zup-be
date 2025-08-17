package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.exception.LostItemException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {

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
}
