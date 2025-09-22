package com.parkmate.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ApiResponse<T>(
        @Schema(description = "Indicates if the request was successful", example = "true")
        boolean success,

        @Schema(description = "Human-readable message", example = "Operation completed successfully")
        String message,

        @Schema(description = "Response data")
        T data,

        @Schema(description = "Error details when success=false")
        ErrorDetails error,

        @Schema(description = "Additional metadata")
        Map<String, Object> meta,

        @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data, null, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data, Map<String, Object> meta) {
        return new ApiResponse<>(true, message, data, null, meta, LocalDateTime.now());
    }

    // ========== ERROR RESPONSES ==========

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, message, null,
                new ErrorDetails(code, message, null, null), null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String code, String message, Map<String, Object> details) {
        return new ApiResponse<>(false, message, null,
                new ErrorDetails(code, message, null, details), null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String code, String message, List<FieldError> fieldErrors) {
        return new ApiResponse<>(false, message, null,
                new ErrorDetails(code, message, fieldErrors, null), null, LocalDateTime.now());
    }

    @Schema(description = "Error details")
    public record ErrorDetails(
            @Schema(description = "Error code", example = "VALIDATION_ERROR")
            String code,

            @Schema(description = "Error message", example = "Validation failed")
            String message,

            @Schema(description = "Field-specific errors")
            List<FieldError> fieldErrors,

            @Schema(description = "Additional error details")
            Map<String, Object> details
    ) {
    }

    @Schema(description = "Field validation error")
    public record FieldError(
            @Schema(description = "Field name", example = "email")
            String field,

            @Schema(description = "Rejected value", example = "invalid-email")
            Object rejectedValue,

            @Schema(description = "Error message", example = "Email format is invalid")
            String message
    ) {
    }
}
