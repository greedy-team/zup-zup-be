package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import org.locationtech.jts.geom.*;

public class SchoolAreaFixture {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public static SchoolArea AI_CENTER() {
        Polygon aiCenter = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.07488219281426, 37.550836719327314),
                new Coordinate(127.07580103474662, 37.55020993929769),
                new Coordinate(127.07605329910534, 37.550673791160946),
                new Coordinate(127.07622635675764, 37.55114445109268),
                new Coordinate(127.07565801942127, 37.55146692343488),
                new Coordinate(127.07488219281426, 37.550836719327314)
        });
        Point marker = geometryFactory.createPoint(
                new Coordinate(127.07555195009019, 37.55064153703773)
        );
        return SchoolArea.builder()
                .areaName("대양 AI 센터")
                .area(aiCenter)
                .marker(marker)
                .build();
    }

    public static SchoolArea PLAYGROUND() {
        Polygon playground = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.07425924633849, 37.550301018817336),
                new Coordinate(127.0751102854006, 37.549771144047334),
                new Coordinate(127.0752944317778, 37.55002330628934),
                new Coordinate(127.07564515695599, 37.54993748728688),
                new Coordinate(127.07579537419866, 37.55020769043576),
                new Coordinate(127.07487936140356, 37.550834468627535),
                new Coordinate(127.07425924633849, 37.550301018817336)
        });
        Point marker = geometryFactory.createPoint(
                new Coordinate(127.0748901701497, 37.55054071738974)
        );
        return SchoolArea.builder()
                .areaName("운동장")
                .area(playground)
                .marker(marker)
                .build();
    }

    public static SchoolArea PLAYGROUND_SIDE_ROAD() {
        Polygon sideRoad = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.07442442479653, 37.55139562757513),
                new Coordinate(127.07406200020375, 37.55110303107704),
                new Coordinate(127.07428815697546, 37.55092494187721),
                new Coordinate(127.07388891845075, 37.55059407555576),
                new Coordinate(127.07426207771036, 37.550303269532236),
                new Coordinate(127.07498412822584, 37.55092224946174),
                new Coordinate(127.07442442479653, 37.55139562757513)
        });
        Point marker = geometryFactory.createPoint(
                new Coordinate(127.07455319705852, 37.55087185)
        );
        return SchoolArea.builder()
                .areaName("중앙 공터")
                .area(sideRoad)
                .marker(marker)
                .build();
    }

    public static SchoolArea STUDENT_HALL() {
        Polygon studentHall = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.07434357008461, 37.54974685181508),
                new Coordinate(127.07442274054304, 37.54970175202579),
                new Coordinate(127.07499979862166, 37.54962029705736),
                new Coordinate(127.0752944317778, 37.55002330628934),
                new Coordinate(127.07563949643331, 37.54993523841736),
                new Coordinate(127.07512939513583, 37.54907961564461),
                new Coordinate(127.07447326792452, 37.54930302839556),
                new Coordinate(127.07441678447164, 37.54940217382835),
                new Coordinate(127.07434357008461, 37.54974685181508)
        });
        Point marker = geometryFactory.createPoint(
                new Coordinate(127.07504531450479, 37.54959835)
        );
        return SchoolArea.builder()
                .areaName("학생회관")
                .area(studentHall)
                .marker(marker)
                .build();
    }
}
