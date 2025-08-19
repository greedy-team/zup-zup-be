package com.greedy.zupzup.category.application;

import com.greedy.zupzup.category.application.dto.CategoryDto;
import com.greedy.zupzup.category.application.dto.FeatureOptionDto;
import com.greedy.zupzup.category.application.dto.FeatureWithOptionsDto;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.exception.CategoryException;
import com.greedy.zupzup.category.presentation.dto.CategoriesResponse;
import com.greedy.zupzup.category.presentation.dto.CategoryFeaturesResponse;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.category.repository.FeatureOptionRepository;
import com.greedy.zupzup.global.exception.ApplicationException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final FeatureOptionRepository featureOptionRepository;

    @Transactional(readOnly = true)
    public CategoriesResponse getAll() {
        List<CategoryDto> list = categoryRepository.findAllByOrderByIdAsc()
                .stream()
                .map(CategoryDto::from)
                .toList();
        return new CategoriesResponse(list);
    }

    @Transactional(readOnly = true)
    public CategoryFeaturesResponse getCategoryFeatures(Long categoryId) {
        Category category = categoryRepository.findWithFeaturesById(categoryId)
                .orElseThrow(() -> new ApplicationException(CategoryException.CATEGORY_NOT_FOUND));

        List<Feature> features = category.getFeatures();
        if (features == null || features.isEmpty()) {
            return CategoryFeaturesResponse.of(category);
        }

        List<Long> featureIdList = features.stream()
                .map(Feature::getId)
                .toList();

        List<FeatureOption> options = featureOptionRepository.findByFeatureIds(featureIdList);

        Map<Long, List<FeatureOptionDto>> optionMap = options.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        fo -> fo.getFeature().getId(),
                        java.util.stream.Collectors.mapping(
                                fo -> new FeatureOptionDto(fo.getId(), fo.getOptionValue()),
                                java.util.stream.Collectors.toList()
                        )
                ));

        List<FeatureWithOptionsDto> featureDtos = features.stream()
                .map(f -> new FeatureWithOptionsDto(
                        f.getId(),
                        f.getName(),
                        f.getQuizQuestion(),
                        optionMap.getOrDefault(f.getId(), List.of())
                ))
                .toList();

        return new CategoryFeaturesResponse(category.getId(), category.getName(), featureDtos);
    }
}
