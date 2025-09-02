package com.greedy.zupzup.lostitem.domain;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.global.BaseTimeEntity;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lost_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LostItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String foundAreaDetail;

    @Column
    private String description;

    @Column(nullable = false)
    private String depositArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LostItemStatus status;

    private LocalDate pledgedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_area_id", nullable = false)
    private SchoolArea foundArea;

    @OneToMany(mappedBy = "lostItem")
    private List<LostItemImage> images = new ArrayList<>();

    public LostItem(String foundAreaDetail, String description, String depositArea, Category category, SchoolArea foundArea) {
        this.foundAreaDetail = foundAreaDetail;
        this.description = description;
        this.depositArea = depositArea;
        this.status = LostItemStatus.REGISTERED;
        this.pledgedAt = null;
        this.category = category;
        this.foundArea = foundArea;
    }

    public boolean isNotQuizCategory() {
        return this.category.isETCategory();
    }

    public boolean isPledgeable() {
        return this.status == LostItemStatus.REGISTERED;
    }

    public void pledge() {
        this.status = LostItemStatus.PLEDGED;
        this.pledgedAt = LocalDate.now();
    }

    public boolean canAccess(boolean pledgedByMe) {
        if (this.status == LostItemStatus.FOUND) return false;
        if (this.status == LostItemStatus.PLEDGED && !pledgedByMe) return false;
        return true;
    }

}
