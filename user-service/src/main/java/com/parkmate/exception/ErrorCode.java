package com.parkmate.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {

    // General errors
    UNCATEGORIZED_EXCEPTION(1000, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1001, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1002, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_ENUM(1003, "Invalid enum", HttpStatus.BAD_REQUEST),

    // User errors 11
    USER_NOT_FOUND(2101, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2102, "User already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(2103, "Email '{0}' is already in use", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS(2104, "Phone number '{0}' is already in use", HttpStatus.CONFLICT),

    // Mobile device errors 12
    DEVICE_NOT_FOUND(2201, "Mobile device not found", HttpStatus.NOT_FOUND),
    DEVICE_ALREADY_REGISTERED(2202, "Device with ID '{0}' is already registered", HttpStatus.CONFLICT),
    DEVICE_NOT_ACTIVE(2203, "Device is not active", HttpStatus.BAD_REQUEST),

    // Vehicle errors 13
    VEHICLE_NOT_FOUND(2301, "Vehicle not found", HttpStatus.NOT_FOUND),
    VEHICLE_ALREADY_EXISTS(2302, "Vehicle with license plate '{0}' already exists", HttpStatus.CONFLICT),

    // Partner errors 14
    PARTNER_NOT_FOUND(2401, "Partner not found", HttpStatus.NOT_FOUND),
    PARTNER_ALREADY_EXISTS(2402, "Partner with name '{0}' already exists", HttpStatus.CONFLICT),
    PARTNER_INACTIVE(2403, "Partner is inactive", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String formatMessage(Object... params) {
        String result = message;
        for (int i = 0; i < params.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(params[i]));
        }
        return result;
    }
}
