package com.greedy.zupzup.auth.application.dto;

public record PortalLoginCommand(
        String portalId,
        String portalPassword
) {
}
