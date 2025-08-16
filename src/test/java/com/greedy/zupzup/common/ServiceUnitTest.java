package com.greedy.zupzup.common;

import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.category.repository.FeatureOptionRepository;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class ServiceUnitTest {

    @Mock
    protected LostItemRepository lostItemRepository;

    @Mock
    protected LostItemImageRepository lostItemImageRepository;

    @Mock
    protected LostItemFeatureRepository lostItemFeatureRepository;

    @Mock
    protected FeatureOptionRepository featureOptionRepository;

    @Mock
    protected SchoolAreaRepository schoolAreaRepository;

    @Mock
    protected CategoryRepository categoryRepository;

    @Mock
    protected S3ImageFileManager s3ImageFileManager;

}
