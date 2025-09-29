package com.parkmate.auth.dto;

import lombok.Builder;

@Builder
public record RefreshRequest(
        String refreshToken
) {
}
