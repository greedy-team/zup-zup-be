package com.greedy.zupzup;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.greedy.zupzup.member.domain.Role;
import com.greedy.zupzup.member.repository.MemberRepository;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.presentation.dto.LatLngResponse;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;

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
    private final ObjectMapper objectMapper;

    private record SchoolAreaInitDto(String areaName, List<List<Double>> coordinates, LatLngResponse marker) {
    }

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

    private void initSchoolAreaData() throws Exception {
        log.info("구역 정보를 초기화합니다.");
        if (schoolAreaRepository.count() == 0) {
            ClassPathResource resource = new ClassPathResource("data/school-areas.json");
            InputStream inputStream = resource.getInputStream();

            List<SchoolAreaInitDto> inputs = objectMapper.readValue(inputStream, new TypeReference<>() {
            });

            List<SchoolArea> schoolAreas = inputs.stream()
                    .map(input -> {
                        Coordinate[] coords = input.coordinates().stream()
                                .map(c -> new Coordinate(c.get(0), c.get(1)))
                                .toArray(Coordinate[]::new);
                        Polygon polygon = geometryFactory.createPolygon(coords);
                        Point marker = geometryFactory.createPoint(new Coordinate(input.marker.lng(), input.marker.lat()));

                        return SchoolArea.builder()
                                .areaName(input.areaName())
                                .area(polygon)
                                .marker(marker)
                                .build();
                    })
                    .collect(Collectors.toList());

            schoolAreaRepository.saveAll(schoolAreas);
            log.info("임시 구역 정보 초기화 완료!");
        } else {
            log.info("구역 정보가 이미 존재하여 초기화를 건너뜁니다.");
        }
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
                    .name("테스트유저")
                    .studentId(123456789)
                    .role(Role.USER)
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
                    .imageOrder(0).lostItem(phoneLostItem).build();
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
                    .imageOrder(0).lostItem(etcLostItem).build();
            lostItemImageRepository.save(umbrellaImage);

            log.info("임시 분실물 정보 초기화 완료!");
        } else {
            log.info("분실물 정보가 이미 존재하여 초기화를 건너뜁니다.");
        }
    }
}
