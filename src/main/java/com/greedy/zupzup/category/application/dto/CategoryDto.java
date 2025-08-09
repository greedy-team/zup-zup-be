package com.greedy.zupzup.category.application.dto;
import com.greedy.zupzup.category.domain.Category;

public record CategoryDto(Long id, String name, String iconUrl) {
    public static CategoryDto from(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getIconUrl());
    }
}
