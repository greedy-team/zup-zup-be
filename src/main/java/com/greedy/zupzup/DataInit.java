package com.greedy.zupzup.schoolarea;


import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final SchoolAreaRepository schoolAreaRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    // 포스트맨 테스트용 임시 데이터 초기화
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 데이터가 이미 존재하면 다시 실행하지 않음
        if (schoolAreaRepository.count() > 0) {
            System.out.println("데이터가 이미 존재하므로 초기화를 건너뜁니다.");
            return;
        }

        System.out.println("데이터베이스에 구역 정보를 초기화합니다...");

        // 1. 세종대 운동장
        Polygon playground = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.0752831, 37.5510817), new Coordinate(127.0742638, 37.5502523),
                new Coordinate(127.0749505, 37.5498356), new Coordinate(127.0759912, 37.5507372),
                new Coordinate(127.0752831, 37.5510817)
        });
        SchoolArea playgroundZone = new SchoolArea("세종대 운동장", playground);


        // 2. 세종대 운동장 옆 길
        Polygon sideRoad = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.0738508, 37.5506351), new Coordinate(127.0742638, 37.5502523),
                new Coordinate(127.075326, 37.5511285), new Coordinate(127.0747198, 37.5516133),
                new Coordinate(127.0740868, 37.5510902), new Coordinate(127.0739902, 37.5510051),
                new Coordinate(127.0741565, 37.5508605), new Coordinate(127.0738508, 37.5506351)
        });
        SchoolArea sideRoadZone = new SchoolArea("세종대 운동장 옆 길", sideRoad);


        // 3. 세종대 AI 센터
        Polygon aiCenter = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.0754923, 37.551137), new Coordinate(127.0757605, 37.5508988),
                new Coordinate(127.076018, 37.5510647), new Coordinate(127.0757015, 37.5512986),
                new Coordinate(127.0754923, 37.551137)
        });
        SchoolArea aiCenterZone = new SchoolArea("세종대 AI 센터", aiCenter);

        // ** (참고) SchoolZone Entity에 생성자가 필요합니다. **
        // public SchoolZone(String areaName, Polygon area) { ... }

        // 한번에 모든 데이터 저장
        schoolAreaRepository.saveAll(Arrays.asList(playgroundZone, sideRoadZone, aiCenterZone));
        System.out.println("구역 정보 초기화 완료.");
    }
}
