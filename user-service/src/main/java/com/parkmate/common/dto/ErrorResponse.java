package com.parkmate.common.dto;

public record ErrorResponse(String code, String message, java.util.Map<String, String> details) {
}

