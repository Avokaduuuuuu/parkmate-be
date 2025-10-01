package com.parkmate.auth.dto;

import lombok.Builder;

@Builder
public record LoginRequest(String email, String password) {
}
