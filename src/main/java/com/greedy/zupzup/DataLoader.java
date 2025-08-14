package com.greedy.zupzup;


import com.greedy.zupzup.category.domain.Category;
import com.greedy.zupzup.category.domain.Feature;
import com.greedy.zupzup.category.domain.FeatureOption;
import com.greedy.zupzup.category.repository.CategoryRepository;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import jakarta.transaction.Transactional;
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
import java.util.List;

@Slf4j
@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final SchoolAreaRepository schoolAreaRepository;
    private final CategoryRepository categoryRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        // ================= 기존 구역 정보 초기화 =================
        initSchoolAreaData();
        // ================= 카테고리 및 관련 정보 초기화 =================
        initCategoryData();
    }

    private void initCategoryData() {
        log.info("카테고리 및 관련 특성 정보를 초기화합니다.");
        if (categoryRepository.count() == 0) {
            // --- 1. 전자기기 카테고리 ---
            Category electronics = Category.builder().name("전자기기").iconUrl("https://cdn-icons-png.flaticon.com/128/519/519184.png").build();

            Feature brand = Feature.builder().name("브랜드").quizQuestion("어떤 브랜드의 제품인가요?").category(electronics).build();
            brand.getOptions().addAll(List.of(
                    FeatureOption.builder().optionValue("삼성").feature(brand).build(),
                    FeatureOption.builder().optionValue("애플").feature(brand).build(),
                    FeatureOption.builder().optionValue("LG").feature(brand).build(),
                    FeatureOption.builder().optionValue("기타").feature(brand).build()
            ));

            Feature color = Feature.builder().name("색상").quizQuestion("제품의 색상은 무엇인가요?").category(electronics).build();
            color.getOptions().addAll(List.of(
                    FeatureOption.builder().optionValue("블랙").feature(color).build(),
                    FeatureOption.builder().optionValue("화이트").feature(color).build(),
                    FeatureOption.builder().optionValue("실버").feature(color).build(),
                    FeatureOption.builder().optionValue("골드").feature(color).build(),
                    FeatureOption.builder().optionValue("기타").feature(color).build()
            ));
            electronics.getFeatures().addAll(List.of(brand, color));


            // --- 2. 지갑/카드 카테고리 ---
            Category wallet = Category.builder().name("지갑/카드").iconUrl("https://cdn-icons-png.flaticon.com/128/4044/4044109.png").build();

            Feature walletType = Feature.builder().name("종류").quizQuestion("어떤 종류의 지갑/카드인가요?").category(wallet).build();
            walletType.getOptions().addAll(List.of(
                    FeatureOption.builder().optionValue("반지갑").feature(walletType).build(),
                    FeatureOption.builder().optionValue("장지갑").feature(walletType).build(),
                    FeatureOption.builder().optionValue("카드지갑").feature(walletType).build(),
                    FeatureOption.builder().optionValue("신분증/면허증").feature(walletType).build(),
                    FeatureOption.builder().optionValue("신용/체크카드").feature(walletType).build()
            ));

            Feature walletColor = Feature.builder().name("색상").quizQuestion("지갑/카드의 색상은 무엇인가요?").category(wallet).build();
            walletColor.getOptions().addAll(List.of(
                    FeatureOption.builder().optionValue("블랙").feature(walletColor).build(),
                    FeatureOption.builder().optionValue("브라운").feature(walletColor).build(),
                    FeatureOption.builder().optionValue("네이비").feature(walletColor).build(),
                    FeatureOption.builder().optionValue("기타").feature(walletColor).build()
            ));
            wallet.getFeatures().addAll(List.of(walletType, walletColor));

            // --- 모든 카테고리 저장 ---
            categoryRepository.saveAll(List.of(electronics, wallet));
            log.info("카테고리 및 관련 특성 정보 초기화 완료!");
        } else {
            log.info("카테고리 정보가 이미 존재하여 초기화를 건너뜁니다.");
        }
    }

    private void initSchoolAreaData() {
        log.info("데이터베이스 임시 구역 정보를 초기화합니다.");
        if (schoolAreaRepository.count() == 0) {
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

            schoolAreaRepository.saveAll(Arrays.asList(playgroundZone, sideRoadZone, aiCenterZone));
            log.info("개발용 임시 구역 정보 초기화 완료!");
        } else {
            log.info("구역 정보가 이미 존재하여 초기화를 건너뜁니다.");
        }
    }
}
