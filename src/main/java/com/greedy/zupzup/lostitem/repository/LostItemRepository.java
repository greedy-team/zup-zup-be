package com.greedy.zupzup.lostitem.repository;

import com.greedy.zupzup.lostitem.domain.LostItem;
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
}
