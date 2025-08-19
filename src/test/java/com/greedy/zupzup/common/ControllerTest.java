package com.greedy.zupzup.common;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.category.repository.FeatureOptionRepository;
import com.greedy.zupzup.category.repository.FeatureRepository;
import com.greedy.zupzup.common.fixture.CategoryFixture;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static com.greedy.zupzup.common.fixture.FeatureFixture.*;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.*;
import static com.greedy.zupzup.common.fixture.SchoolAreaFixture.*;

@Sql("/truncate.sql")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ControllerTest {

    @MockitoBean
    protected S3ImageFileManager imageFileManager;

    @Autowired
    protected LostItemRepository lostItemRepository;

    @Autowired
    protected LostItemFeatureRepository lostItemFeatureRepository;

    @Autowired
    protected LostItemImageRepository lostItemImageRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected FeatureRepository featureRepository;

    @Autowired
    protected FeatureOptionRepository featureOptionRepository;

    @Autowired
    protected SchoolAreaRepository schoolAreaRepository;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    protected List<SchoolArea> givenSchoolAreas() {
        SchoolArea playground = PLAYGROUND();
        SchoolArea aiCenter = AI_CENTER();
        SchoolArea sideRoad = PLAYGROUND_SIDE_ROAD();
        List<SchoolArea> schoolAreas = List.of(playground, aiCenter, sideRoad);
        schoolAreaRepository.saveAll(schoolAreas);
        return schoolAreas;
    }

    protected Category givenElectronicsCategory() {
        Category electronic = categoryRepository.save(CategoryFixture.ELECTRONIC());

        Feature brandFeature = ELECTRONIC_BRAND(electronic);
        Feature colorFeature = ELECTRONIC_COLOR(electronic);
        featureRepository.save(brandFeature);
        featureRepository.save(colorFeature);

        List<FeatureOption> brandOptions = ELECTRONIC_BRAND_OPTIONS(brandFeature);
        List<FeatureOption> colorOptions = ELECTRONIC_COLOR_OPTIONS(colorFeature);
        featureOptionRepository.saveAll(brandOptions);
        featureOptionRepository.saveAll(colorOptions);

        brandFeature.getOptions().addAll(brandOptions);
        colorFeature.getOptions().addAll(colorOptions);
        electronic.getFeatures().addAll(List.of(brandFeature, colorFeature));
        return electronic;
    }

    protected Category givenWalletCategory() {
        Category wallet = categoryRepository.save(CategoryFixture.WALLET());

        Feature typeFeature = WALLET_TYPE(wallet);
        Feature colorFeature = WALLET_COLOR(wallet);
        featureRepository.save(typeFeature);
        featureRepository.save(colorFeature);

        List<FeatureOption> typeOptions = WALLET_TYPE_OPTIONS(typeFeature);
        List<FeatureOption> colorOptions = WALLET_COLOR_OPTIONS(colorFeature);
        featureOptionRepository.saveAll(typeOptions);
        featureOptionRepository.saveAll(colorOptions);

        typeFeature.getOptions().addAll(typeOptions);
        colorFeature.getOptions().addAll(colorOptions);
        wallet.getFeatures().addAll(List.of(typeFeature, colorFeature));
        return wallet;
    }

}
