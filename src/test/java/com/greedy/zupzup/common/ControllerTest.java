package com.greedy.zupzup.global;

import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.category.repository.FeatureOptionRepository;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

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
    protected FeatureOptionRepository featureOptionRepository;

    @Autowired
    protected SchoolAreaRepository schoolAreaRepository;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }
}
