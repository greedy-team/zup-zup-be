package com.greedy.zupzup.schoolarea.presentation;

import com.greedy.zupzup.schoolarea.application.SchoolAreaService;
import com.greedy.zupzup.schoolarea.domain.SchoolArea;
import com.greedy.zupzup.schoolarea.presentation.dto.AllSchoolAreasResponse;
import com.greedy.zupzup.schoolarea.presentation.dto.LatLngRequest;
import com.greedy.zupzup.schoolarea.presentation.dto.SchoolAreaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/school-areas")
public class SchoolAreaController implements SchoolAreaControllerDocs {

    private final SchoolAreaService schoolAreaService;

    @GetMapping("/contains")
    public ResponseEntity<SchoolAreaResponse> findArea(@ParameterObject @Valid LatLngRequest request) {
        SchoolArea findArea = schoolAreaService.findByLatLng(request.toCommand());
        return ResponseEntity.ok(SchoolAreaResponse.from(findArea));
    }

    @GetMapping
    public ResponseEntity<AllSchoolAreasResponse> findAll() {
        List<SchoolArea> allAreas = schoolAreaService.findAllAreas();
        return ResponseEntity.ok(AllSchoolAreasResponse.of(allAreas));
    }
}
