package com.greedy.zupzup.lostitem.presentation;

import com.greedy.zupzup.lostitem.application.LostItemSimpleViewService;
import com.greedy.zupzup.lostitem.application.dto.LostItemSimpleViewCommand;
import com.greedy.zupzup.lostitem.presentation.dto.LostItemSimpleViewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lost-items")
public class LostItemSimpleViewController {

    private final LostItemSimpleViewService service;

    @GetMapping("/{lostItemId}")
    public ResponseEntity<LostItemSimpleViewResponse> getBasic(@PathVariable Long lostItemId) {
        LostItemSimpleViewCommand command = service.getBasic(lostItemId);
        return ResponseEntity.ok(LostItemSimpleViewResponse.from(command));
    }
}
