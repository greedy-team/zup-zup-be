package com.greedy.zupzup.schoolarea.application;

import com.greedy.zupzup.schoolarea.application.dto.FindAreaCommand;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.repository.SchoolAreaRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolAreaService {

    private final SchoolAreaRepository schoolAreaRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Transactional(readOnly = true)
    public SchoolArea findByLatLng(FindAreaCommand command) {
        Point userPoint = geometryFactory.createPoint(new Coordinate(command.lng(), command.lat()));
        return schoolAreaRepository.getZoneByPoint(userPoint);
    }

    @Transactional(readOnly = true)
    public List<SchoolArea> findAllAreas() {
        return schoolAreaRepository.findAll();
    }
}
