package com.parkmate.dto.response;

public record ErrorResponse(String code, String message, java.util.Map<String, String> details) {
}

