package com.greedy.zupzup.category.presentation.dto;

import com.greedy.zupzup.category.application.dto.CategoryDto;
import java.util.List;

public record CategoriesResponse(List<CategoryDto> categories) {}
