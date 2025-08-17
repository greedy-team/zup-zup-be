package com.greedy.zupzup;

import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.lostitem.domain.LostItem;
import com.greedy.zupzup.lostitem.domain.LostItemFeature;
import com.greedy.zupzup.lostitem.domain.LostItemImage;
import com.greedy.zupzup.lostitem.domain.LostItemStatus;
import com.greedy.zupzup.lostitem.repository.LostItemFeatureRepository;
import com.greedy.zupzup.lostitem.repository.LostItemImageRepository;
import com.greedy.zupzup.lostitem.repository.LostItemRepository;
import com.greedy.zupzup.member.domain.Member;
import com.greedy.zupzup.member.domain.Provider;
import com.greedy.zupzup.member.domain.Role;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final SchoolAreaRepository schoolAreaRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final LostItemRepository lostItemRepository;
    private final LostItemFeatureRepository lostItemFeatureRepository;
    private final LostItemImageRepository lostItemImageRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("========== 개발용 임시 데이터 초기화 시작 ==========");
        initSchoolAreaData();
        initCategoryData();
        initMemberData();
        initLostItemData();
        log.info("========== 개발용 임시 데이터 초기화 완료 ==========");
    }

    private void initSchoolAreaData() {
        log.info("구역 정보를 초기화합니다.");
        if (schoolAreaRepository.count() == 0) {
            Polygon playground = geometryFactory.createPolygon(
                    new Coordinate[]{new Coordinate(127.0752831, 37.5510817), new Coordinate(127.0742638, 37.5502523),
                            new Coordinate(127.0749505, 37.5498356), new Coordinate(127.0759912, 37.5507372),
                            new Coordinate(127.0752831, 37.5510817)});
            SchoolArea playgroundZone = SchoolArea.builder().areaName("세종대학교 운동장").area(playground).build();

            Polygon sideRoad = geometryFactory.createPolygon(
                    new Coordinate[]{new Coordinate(127.0738508, 37.5506351), new Coordinate(127.0742638, 37.5502523),
                            new Coordinate(127.075326, 37.5511285), new Coordinate(127.0747198, 37.5516133),
                            new Coordinate(127.0740868, 37.5510902), new Coordinate(127.0739902, 37.5510051),
                            new Coordinate(127.0741565, 37.5508605), new Coordinate(127.0738508, 37.5506351)});
            SchoolArea sideRoadZone = SchoolArea.builder().areaName("세종대학교 운동장 옆 길").area(sideRoad).build();

            Polygon aiCenter = geometryFactory.createPolygon(
                    new Coordinate[]{new Coordinate(127.0754923, 37.551137), new Coordinate(127.0757605, 37.5508988),
                            new Coordinate(127.076018, 37.5510647), new Coordinate(127.0757015, 37.5512986),
                            new Coordinate(127.0754923, 37.551137)});
            SchoolArea aiCenterZone = SchoolArea.builder().areaName("세종대학교 AI 센터").area(aiCenter).build();

            schoolAreaRepository.saveAll(Arrays.asList(playgroundZone, sideRoadZone, aiCenterZone));
            log.info("임시 구역 정보 초기화 완료!");
        } else {
            log.info("구역 정보가 이미 존재하여 초기화를 건너뜁니다.");
        }
    }

    private List<SchoolArea> createSchoolAreas() {
        Polygon playground = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.0752831, 37.5510817), new Coordinate(127.0742638, 37.5502523),
                new Coordinate(127.0749505, 37.5498356), new Coordinate(127.0759912, 37.5507372),
                new Coordinate(127.0752831, 37.5510817)
        });
        SchoolArea playgroundZone = new SchoolArea("세종대학교 운동장", playground);

        Polygon sideRoad = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.0738508, 37.5506351), new Coordinate(127.0742638, 37.5502523),
                new Coordinate(127.075326, 37.5511285), new Coordinate(127.0747198, 37.5516133),
                new Coordinate(127.0740868, 37.5510902), new Coordinate(127.0739902, 37.5510051),
                new Coordinate(127.0741565, 37.5508605), new Coordinate(127.0738508, 37.5506351)
        });
        SchoolArea sideRoadZone = new SchoolArea("세종대학교 운동장 옆 길", sideRoad);

        Polygon aiCenter = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.0754923, 37.551137), new Coordinate(127.0757605, 37.5508988),
                new Coordinate(127.076018, 37.5510647), new Coordinate(127.0757015, 37.5512986),
                new Coordinate(127.0754923, 37.551137)
        });
        SchoolArea aiCenterZone = new SchoolArea("세종대학교 AI 센터", aiCenter);

        return schoolAreaRepository.saveAll(Arrays.asList(playgroundZone, sideRoadZone, aiCenterZone));
    }

    private void initCategoryData() {
        log.info("카테고리 정보를 초기화합니다.");
        if (categoryRepository.count() == 0) {
            // --- 1. 핸드폰 카테고리 ---
            Category phoneCategory = Category.builder().name("핸드폰").iconUrl("http://example.com/phone-icon.png")
                    .build();
            Feature brand = Feature.builder().name("브랜드").quizQuestion("어떤 브랜드의 제품인가요?").category(phoneCategory)
                    .build();
            brand.getOptions().addAll(List.of(
                    FeatureOption.builder().optionValue("삼성").feature(brand).build(),
                    FeatureOption.builder().optionValue("애플").feature(brand).build(),
                    FeatureOption.builder().optionValue("LG").feature(brand).build(),
                    FeatureOption.builder().optionValue("기타").feature(brand).build()
            ));
            Feature color = Feature.builder().name("색상").quizQuestion("제품의 색상은 무엇인가요?").category(phoneCategory).build();
            color.getOptions().addAll(List.of(
                    FeatureOption.builder().optionValue("블랙").feature(color).build(),
                    FeatureOption.builder().optionValue("화이트").feature(color).build(),
                    FeatureOption.builder().optionValue("실버").feature(color).build(),
                    FeatureOption.builder().optionValue("골드").feature(color).build(),
                    FeatureOption.builder().optionValue("기타").feature(color).build()
            ));
            phoneCategory.getFeatures().addAll(List.of(brand, color));

            // --- 2. 기타 카테고리 ---
            Category etcCategory = Category.builder().name("기타").iconUrl("http://example.com/etc-icon.png").build();

            categoryRepository.saveAll(List.of(phoneCategory, etcCategory));
            log.info("카테고리 정보 초기화 완료!");
        } else {
            log.info("카테고리 정보가 이미 존재하여 초기화를 건너뜁니다.");
        }
    }

    private void initMemberData() {
        log.info("회원 정보를 초기화합니다.");
        if (memberRepository.count() == 0) {
            Member member = Member.builder()
                    .email("testuser@example.com")
                    .nickname("테스트유저")
                    .provider(Provider.GOOGLE)
                    .providerId("123456789")
                    .role(Role.USER)
                    .emailConsent(true)
                    .build();
            memberRepository.save(member);
            log.info("임시 회원 정보 초기화 완료!");
        } else {
            log.info("회원 정보가 이미 존재하여 초기화를 건너뜁니다.");
        }
    }

    private void initLostItemData() {
        log.info("분실물 정보를 초기화합니다.");
        if (lostItemRepository.count() == 0) {
            Member member = memberRepository.findAll().get(0);
            List<Category> categories = categoryRepository.findAll();
            Category phoneCategory = categories.get(0);
            Category etcCategory = categories.get(1);
            List<SchoolArea> schoolAreas = schoolAreaRepository.findAll();
            SchoolArea aiCenter = schoolAreas.get(2);
            SchoolArea playground = schoolAreas.get(0);

            // --- 1. 퀴즈가 있는 분실물 생성 (핸드폰) ---
            LostItem phoneLostItem = LostItem.builder()
                    .foundAreaDetail("AI 센터 1층 로비 소파 위")
                    .description("검정색 아이폰 15 프로입니다. 케이스는 투명색입니다.")
                    .depositArea("AI 센터 1층 안내데스크")
                    .status(LostItemStatus.REGISTERED)
                    .category(phoneCategory)
                    .foundArea(aiCenter)
                    .build();
            lostItemRepository.save(phoneLostItem);

            LostItemImage phoneImage = LostItemImage.builder()
                    .imageKey("https://zupzup-static-files.s3.ap-northeast-2.amazonaws.com/dev/iphone.webp")
                    .imageOrder(1).lostItem(phoneLostItem).build();
            lostItemImageRepository.save(phoneImage);

            Feature brandFeature = phoneCategory.getFeatures().get(0);
            FeatureOption appleOption = brandFeature.getOptions().get(1);
            LostItemFeature lostItemBrand = LostItemFeature.builder().lostItem(phoneLostItem).feature(brandFeature)
                    .selectedOption(appleOption).build();

            Feature colorFeature = phoneCategory.getFeatures().get(1);
            FeatureOption blackOption = colorFeature.getOptions().get(0);
            LostItemFeature lostItemColor = LostItemFeature.builder().lostItem(phoneLostItem).feature(colorFeature)
                    .selectedOption(blackOption).build();
            lostItemFeatureRepository.saveAll(List.of(lostItemBrand, lostItemColor));

            // --- 2. 퀴즈가 없는 분실물 생성 (기타) ---
            LostItem etcLostItem = LostItem.builder()
                    .foundAreaDetail("운동장 스탠드 세 번째 줄")
                    .description("검정색 3단 우산. 손잡이에 곰돌이 스티커가 붙어있음.")
                    .depositArea("학생회관 분실물 보관함")
                    .status(LostItemStatus.REGISTERED)
                    .category(etcCategory)
                    .foundArea(playground)
                    .build();
            lostItemRepository.save(etcLostItem);

            LostItemImage umbrellaImage = LostItemImage.builder()
                    .imageKey("https://zupzup-static-files.s3.ap-northeast-2.amazonaws.com/dev/umbrella.webp")
                    .imageOrder(1).lostItem(etcLostItem).build();
            lostItemImageRepository.save(umbrellaImage);

            log.info("임시 분실물 정보 초기화 완료!");
        } else {
            log.info("분실물 정보가 이미 존재하여 초기화를 건너뜁니다.");
        }
    }
}
