package com.greedy.zupzup.category.application;

import com.greedy.zupzup.category.application.dto.CategoryDto;
import com.greedy.zupzup.category.application.dto.FeatureOptionDto;
import com.greedy.zupzup.category.application.dto.FeatureWithOptionsDto;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.category.presentation.dto.CategoryFeaturesResponse;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.category.repository.FeatureOptionRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;
    private final FeatureOptionRepository featureOptionRepository;

    @Transactional(readOnly = true)
    public CategoriesResponse getAll() {
        var list = categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CategoryDto::from)
                .toList();
        return new CategoriesResponse(list);
    }

    @Transactional(readOnly = true)
    public CategoryFeaturesResponse getCategoryFeatures(Long categoryId) {
        var category = categoryRepository.findWithFeaturesById(categoryId)
                .orElseThrow(() -> new ApplicationException(CategoryException.CATEGORY_NOT_FOUND));

        var features = category.getFeatures();
        if (features == null || features.isEmpty()) {
            return CategoryFeaturesResponse.of(category);
        }

        var featureIdList = features.stream().map(Feature::getId).toList();

        var options = featureOptionRepository.findByFeatureIds(featureIdList);

        var optionMap = options.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        fo -> fo.getFeature().getId(),
                        java.util.stream.Collectors.mapping(
                                fo -> new FeatureOptionDto(fo.getId(), fo.getOptionValue()),
                                java.util.stream.Collectors.toList()
                        )
                ));

        var featureDtos = features.stream()
                .map(f -> new FeatureWithOptionsDto(
                        f.getId(),
                        f.getName(),
                        f.getQuizQuestion(),
                        optionMap.getOrDefault(f.getId(), java.util.List.of())
                ))
                .toList();

        return new CategoryFeaturesResponse(category.getId(), category.getName(), featureDtos);
    }
}
