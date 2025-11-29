package com.greedy.zupzup.common;

import com.greedy.zupzup.auth.infrastructure.SejongAuthenticator;
import com.greedy.zupzup.auth.jwt.JwtTokenProvider;
import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.category.repository.FeatureOptionRepository;
import com.greedy.zupzup.category.repository.FeatureRepository;
import com.greedy.zupzup.common.fixture.CategoryFixture;
import com.greedy.zupzup.global.infrastructure.S3ImageFileManager;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Role;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.pledge.repository.PledgeRepository;
import com.greedy.zupzup.quiz.repository.QuizAttemptRepository;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import io.restassured.RestAssured;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static com.greedy.zupzup.common.fixture.FeatureFixture.*;
import static com.greedy.zupzup.common.fixture.FeatureOptionFixture.*;
import static com.greedy.zupzup.common.fixture.LostItemImageFixture.*;
import static com.greedy.zupzup.common.fixture.MemberFixture.*;
import static com.greedy.zupzup.common.fixture.SchoolAreaFixture.*;

@Sql("/truncate.sql")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ControllerTest {

    @MockitoBean
    protected S3ImageFileManager imageFileManager;

    @MockitoBean
    protected SejongAuthenticator sejongAuthenticator;

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

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected QuizAttemptRepository quizAttemptRepository;

    @Autowired
    protected PledgeRepository pledgeRepository;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    @Autowired
    protected CacheManager cacheManager;

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    protected String givenAccessToken(Member member) {
        return jwtTokenProvider.createAccessToken(member);
    }

    protected Member givenMember(String password) {
        Member member = MEMBER_WITH_ENCODED_PASSWORD(password);
        return memberRepository.save(member);
    }

    protected Member givenMember(Integer studentId, String password) {
        Member member = Member.builder()
                .studentId(studentId)
                .password(password)
                .name("테스트유저" + studentId)
                .role(Role.USER)
                .build();
        return memberRepository.save(member);
    }

    protected Member givenAdmin(String password) {
        Member member = ADMIN_WITH_ENCODED_PASSWORD(password);
        return memberRepository.save(member);
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

    protected Category givenEtcCategory() {
        return categoryRepository.save(CategoryFixture.ETC());
    }

    protected LostItem givenRegisteredLostItem(Category category) {
        SchoolArea schoolArea = schoolAreaRepository.save(AI_CENTER());

        LostItem lostItem = LostItem.builder()
                .foundAreaDetail("AI 센터 B205")
                .description("검정색 아이폰 15 프로")
                .depositArea("학술정보원 2층 데스크")
                .category(category)
                .status(LostItemStatus.REGISTERED)
                .foundArea(schoolArea)
                .foundAt(null)
                .build();

        lostItemRepository.save(lostItem);
        lostItemImageRepository.save(DEFAULT_IMAGE(lostItem));

        // 1. 브랜드 특징 및 정답 설정
        Feature brandFeature = category.getFeatures().stream()
                .filter(f -> f.getName().equals("브랜드")).findFirst().orElseThrow();
        FeatureOption selectedBrandOption = brandFeature.getOptions().get(0); // 삼성이 정답
        lostItemFeatureRepository.save(new LostItemFeature(lostItem, brandFeature, selectedBrandOption));

        // 2. 색상 특징 및 정답 설정
        Feature colorFeature = category.getFeatures().stream()
                .filter(f -> f.getName().equals("색상")).findFirst().orElseThrow();
        FeatureOption selectedColorOption = colorFeature.getOptions().get(0); // 블랙이 정답
        lostItemFeatureRepository.save(new LostItemFeature(lostItem, colorFeature, selectedColorOption));

        return lostItem;
    }

    protected LostItem givenNonQuizLostItem(Category category) {
        SchoolArea schoolArea = schoolAreaRepository.save(AI_CENTER());

        LostItem lostItem = LostItem.builder()
                .foundAreaDetail("학생회관 1층")
                .description("갈색 곰인형 키링")
                .depositArea("학생회관 1층 분실물 보관소")
                .category(category)
                .status(LostItemStatus.REGISTERED)
                .foundArea(schoolArea)
                .foundAt(null)
                .build();

        lostItemRepository.save(lostItem);
        lostItemImageRepository.save(DEFAULT_IMAGE(lostItem));

        return lostItem;
    }

    protected LostItem givenRegisteredLostItemInArea(Category category, SchoolArea area) {

        LostItem lostItem = LostItem.builder()
                .foundAreaDetail(area.getAreaName() + " 1층")
                .description("테스트 분실물")
                .depositArea("보관소")
                .category(category)
                .status(LostItemStatus.REGISTERED)
                .foundArea(area)
                .foundAt(null)
                .build();

        lostItemRepository.save(lostItem);
        return lostItem;
    }


    public void givenLostItemImages(Long lostItemId, List<String> imageKeys) {
        for (int i = 0; i < imageKeys.size(); i++) {
            givenLostItemImage(lostItemId, imageKeys.get(i), "url_" + i, i == 0);
        }
    }

    protected LostItem givenPendingLostItem(Category category) {
        SchoolArea area = schoolAreaRepository.save(AI_CENTER());

        LostItem lostItem = LostItem.builder()
                .category(category)
                .foundArea(area)
                .foundAreaDetail("상세위치")
                .depositArea("보관장소")
                .description("test item")
                .status(LostItemStatus.PENDING)
                .build();

        return lostItemRepository.save(lostItem);
    }


    protected void givenLostItemImage(Long lostItemId, String imageKey, String imageUrl, boolean isRepresentative) {
        LostItem lostItemRef = lostItemRepository.getById(lostItemId);

        lostItemImageRepository.save(
                SPECIFIC_IMAGE(lostItemRef, imageKey, imageUrl, isRepresentative)
        );
    }

    protected List<LostItem> givenMultiplePendingLostItemsWithImages(Category category, int count) {
        List<LostItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            LostItem item = givenPendingLostItem(category);
            givenLostItemImages(item.getId(), List.of("img1_" + item.getId(), "img2_" + item.getId()));
            items.add(item);
        }
        return items;
    }

    protected LostItem givenPendingLostItemWithFeatures(Category category) {
        LostItem item = givenPendingLostItem(category);

        Feature brandFeature = category.getFeatures().stream()
                .filter(f -> f.getName().equals("브랜드")).findFirst().orElseThrow();
        FeatureOption brand = brandFeature.getOptions().get(0);
        lostItemFeatureRepository.save(new LostItemFeature(item, brandFeature, brand));

        Feature colorFeature = category.getFeatures().stream()
                .filter(f -> f.getName().equals("색상")).findFirst().orElseThrow();
        FeatureOption color = colorFeature.getOptions().get(0);
        lostItemFeatureRepository.save(new LostItemFeature(item, colorFeature, color));

        return item;
    }
}
