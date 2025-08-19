package com.greedy.zupzup.common.fixture;

import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

public class SchoolAreaFixture {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public static SchoolArea AI_CENTER() {
        Polygon aiCenter = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.0754923, 37.551137), new Coordinate(127.0757605, 37.5508988),
                new Coordinate(127.076018, 37.5510647), new Coordinate(127.0757015, 37.5512986),
                new Coordinate(127.0754923, 37.551137)
        });
        return new SchoolArea("세종대학교 AI 센터", aiCenter);
    }

    public static SchoolArea PLAYGROUND() {
        Polygon playground = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.0752831, 37.5510817), new Coordinate(127.0742638, 37.5502523),
                new Coordinate(127.0749505, 37.5498356), new Coordinate(127.0759912, 37.5507372),
                new Coordinate(127.0752831, 37.5510817)
        });
        return new SchoolArea("세종대학교 운동장", playground);
    }

    public static SchoolArea PLAYGROUND_SIDE_ROAD() {
        Polygon sideRoad = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(127.0738508, 37.5506351), new Coordinate(127.0742638, 37.5502523),
                new Coordinate(127.075326, 37.5511285), new Coordinate(127.0747198, 37.5516133),
                new Coordinate(127.0740868, 37.5510902), new Coordinate(127.0739902, 37.5510051),
                new Coordinate(127.0741565, 37.5508605), new Coordinate(127.0738508, 37.5506351)
        });
        return new SchoolArea("세종대학교 운동장 옆 길", sideRoad);
    }

}
