package com.greedy.zupzup.schoolarea.repository;

import com.greedy.zupzup.global.exception.ApplicationException;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.exception.SchoolAreaException;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SchoolAreaRepository extends JpaRepository<SchoolArea, Long> {

    @Query(value = "SELECT sa FROM SchoolArea sa WHERE ST_Contains(sa.area, :point) = true")
    Optional<SchoolArea> findZoneByPoint(@Param("point") Point point);

    default SchoolArea getZoneByPoint(Point point) {
        return findZoneByPoint(point)
                .orElseThrow(() -> new ApplicationException(SchoolAreaException.SCHOOL_AREA_NOT_FOUND));
    }
}
