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
import java.time.LocalDateTime;
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

    private LocalDateTime foundAt;

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
        this.status = LostItemStatus.PENDING;
        this.foundAt = null;
        this.category = category;
        this.foundArea = foundArea;
    }

    public boolean isEtcCategory() {
        return this.category.isEtcCategory();
    }

    public boolean isPledgeable() {
        return this.status == LostItemStatus.REGISTERED;
    }

    public void pledge() {
        this.status = LostItemStatus.PLEDGED;
    }

    public void found() {
        this.status = LostItemStatus.FOUND;
        this.foundAt = LocalDateTime.now();
    }

    public boolean canAccess(boolean pledgedByMe) {
        if (this.status == LostItemStatus.FOUND) return false;
        if (this.status == LostItemStatus.PLEDGED && !pledgedByMe) return false;
        return true;
    }

    public void changeStatus(LostItemStatus status) {
        this.status = status;
    }

    public void cancelPledge() {
        if (this.status != LostItemStatus.PLEDGED) {
            throw new IllegalStateException("취소할 수 있는 상태가 아닙니다.");
        }
        this.status = LostItemStatus.REGISTERED;
    }

    public void completeFound() {
        if (this.status != LostItemStatus.PLEDGED) {
            throw new IllegalStateException("습득 완료할 수 있는 상태가 아닙니다.");
        }
        this.status = LostItemStatus.FOUND;
        this.foundAt = LocalDateTime.now();
    }

    public boolean isPledged() {
        return this.status == LostItemStatus.PLEDGED;
    }

}
