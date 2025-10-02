package com.parkmate.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public enum ErrorCode {

    // General errors
    UNCATEGORIZED_EXCEPTION(1000, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1001, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1002, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_ENUM(1003, "Invalid enum", HttpStatus.BAD_REQUEST),

    // User errors 21
    USER_NOT_FOUND(2101, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(2102, "User already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(2103, "Email '{0}' is already in use", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS(2104, "Phone number '{0}' is already in use", HttpStatus.CONFLICT),
    USER_NAME_ALREADY_EXISTS(2105, "Username '{0}' is already in use", HttpStatus.CONFLICT),

    // Mobile device errors 22
    DEVICE_NOT_FOUND(2201, "Mobile device not found", HttpStatus.NOT_FOUND),
    DEVICE_ALREADY_REGISTERED(2202, "Device with ID '{0}' is already registered", HttpStatus.CONFLICT),
    DEVICE_NOT_ACTIVE(2203, "Device is not active", HttpStatus.BAD_REQUEST),

    // Vehicle errors 23
    VEHICLE_NOT_FOUND(2301, "Vehicle not found", HttpStatus.NOT_FOUND),
    VEHICLE_ALREADY_EXISTS(2302, "Vehicle with license plate '{0}' already exists", HttpStatus.CONFLICT),

    // Partner errors 24
    PARTNER_NOT_FOUND(2401, "Partner not found", HttpStatus.NOT_FOUND),
    PARTNER_ALREADY_EXISTS(2402, "Partner with name '{0}' already exists", HttpStatus.CONFLICT),
    PARTNER_INACTIVE(2403, "Partner is inactive", HttpStatus.BAD_REQUEST),

    // Account errors 25
    ACCOUNT_NOT_FOUND(2501, "Account not found", HttpStatus.NOT_FOUND),
    ACCOUNT_ALREADY_EXISTS(2502, "Account with email '{0}' already exists", HttpStatus.CONFLICT),
    ACCOUNT_INACTIVE(2503, "Account is inactive", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_VERIFIED(2504, "Email has been verified before on this account", HttpStatus.CONFLICT),
    EMAIL_RESEND_FAILED(2505, "Failed to resend verification email", HttpStatus.UNAUTHORIZED),

    // Partner Registration errors 27
    TAX_NUMBER_ALREADY_EXISTS(2701, "A partner with the same tax number '{0}' already exists.", HttpStatus.CONFLICT),
    PARTNER_REGISTRATION_NOT_FOUND(2702, "Partner registration request not found", HttpStatus.NOT_FOUND),
    PARTNER_REGISTRATION_ALREADY_REVIEWED(2703, "This partner registration request has already been reviewed and cannot be modified.", HttpStatus.BAD_REQUEST),

    // Auth error 26
    PASSWORD_MISMATCH(2601, "Password does not match", HttpStatus.UNAUTHORIZED),
    USER_INFO_NOT_FOUND(2602, "User information not found", HttpStatus.NOT_FOUND),
    STORE_REFRESH_TOKEN_FAILED(2603, "Failed to store refresh token", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REFRESH_TOKEN(2604, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    INVALID_VALIDATION_CODE(2605, "Verify code not match", HttpStatus.BAD_REQUEST);

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
