package com.greedy.zupzup.lostitem.presentation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelPledgeResponse {
    private Long lostItemId;
    private String status;
    private String message;
}
