package com.parkmate.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {

    // General errors
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1001, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1002, "Invalid request", HttpStatus.BAD_REQUEST),

    // User errors
    USER_NOT_FOUND(1101, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1102, "User already exists", HttpStatus.CONFLICT),

    // Mobile device errors
    DEVICE_NOT_FOUND(1201, "Mobile device not found", HttpStatus.NOT_FOUND),
    DEVICE_ALREADY_REGISTERED(1202, "Device with ID '{0}' is already registered", HttpStatus.CONFLICT),
    DEVICE_NOT_ACTIVE(1203, "Device is not active", HttpStatus.BAD_REQUEST),

    // Vehicle errors
    VEHICLE_NOT_FOUND(1301, "Vehicle not found", HttpStatus.NOT_FOUND),
    VEHICLE_ALREADY_EXISTS(1302, "Vehicle with license plate '{0}' already exists", HttpStatus.CONFLICT);

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
