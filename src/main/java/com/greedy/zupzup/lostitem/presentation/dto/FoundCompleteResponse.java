package com.greedy.zupzup.lostitem.presentation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FoundCompleteResponse {
    private Long lostItemId;
    private String status;
    private String message;
}
