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

    @Query(value = """
            select sa
            from SchoolArea sa
            where ST_Contains(sa.area, :point) = true
            """)
    Optional<SchoolArea> findSchoolAreaByPoint(@Param("point") Point point);

    default SchoolArea getSchoolAreaByPoint(Point point) {
        return findSchoolAreaByPoint(point)
                .orElseThrow(() -> new ApplicationException(SchoolAreaException.SCHOOL_AREA_OUT_OF_BOUND));
    }

    default SchoolArea getAreaById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ApplicationException(SchoolAreaException.SCHOOL_AREA_NOT_FOUND));
    }
}
