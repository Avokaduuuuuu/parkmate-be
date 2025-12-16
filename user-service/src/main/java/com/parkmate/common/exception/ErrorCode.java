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
    IO_ERROR(1004, "I/O error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1005, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    OTHER_CLIENT_ERROR(1006, "Other client error", HttpStatus.BAD_REQUEST),

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
    INVALID_VEHICLE_TYPE(2303, "Invalid vehicle type '{0}'", HttpStatus.BAD_REQUEST),
    VEHICLE_NOT_BELONG_TO_USER(2304, "Vehicle does not belong to the user", HttpStatus.BAD_REQUEST),

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
    ACCOUNT_NOT_VERIFIED(2506, "Please verify email before logging in", HttpStatus.UNAUTHORIZED),
    ACCOUNT_TEMPORARILY_LOCKED(2507, "Tài khoản tạm thời bị khóa. Vui lòng thử lại sau {0} phút", HttpStatus.TOO_MANY_REQUESTS),

    // Partner Registration errors 27
    TAX_NUMBER_ALREADY_EXISTS(2701, "A partner with the same tax number '{0}' already exists.", HttpStatus.CONFLICT),
    PARTNER_REGISTRATION_NOT_FOUND(2702, "Partner registration request not found", HttpStatus.NOT_FOUND),
    PARTNER_REGISTRATION_ALREADY_REVIEWED(2703, "This partner registration request has already been reviewed and cannot be modified.", HttpStatus.BAD_REQUEST),

    // Auth error 26
    PASSWORD_MISMATCH(2601, "Password does not match", HttpStatus.UNAUTHORIZED),
    USER_INFO_NOT_FOUND(2602, "User information not found", HttpStatus.NOT_FOUND),
    STORE_REFRESH_TOKEN_FAILED(2603, "Failed to store refresh token", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REFRESH_TOKEN(2604, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    INVALID_VALIDATION_CODE(2605, "Verify code not match", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_RESET_CODE(2606, "Mã đặt lại mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_CODE_EXPIRED(2607, "Mã đặt lại mật khẩu đã hết hạn", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH_WITH_ATTEMPTS(2608, "Sai mật khẩu. Còn {0} lần thử", HttpStatus.UNAUTHORIZED),

    // File Upload Error 28
    FILE_EMPTY(2801, "Uploaded file is empty", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(2802, "Uploaded file size exceeds the limit", HttpStatus.BAD_REQUEST),
    FILE_TYPE_NOT_ALLOWED(2803, "Uploaded file type is not allowed", HttpStatus.BAD_REQUEST),
    S3_UPLOAD_FAILED(2804, "Failed to upload file to S3", HttpStatus.INTERNAL_SERVER_ERROR),

    //Wallet errors 32
    WALLET_NOT_FOUND(3201, "Wallet not found", HttpStatus.NOT_FOUND),
    WALLET_ALREADY_EXISTS(3202, "Wallet with ID '{0}' already exists", HttpStatus.CONFLICT),
    INSUFFICIENT_WALLET_BALANCE(3203, "Insufficient wallet balance", HttpStatus.BAD_REQUEST),
    WALLET_TOPUP_FAILED(3204, "Wallet top-up failed", HttpStatus.INTERNAL_SERVER_ERROR),
    WALLET_TRANSACTION_NOT_FOUND(3205, "Wallet transaction not found", HttpStatus.NOT_FOUND),
    WALLET_TRANSACTION_ALREADY_EXISTS(3206, "Wallet transaction with ID '{0}' already exists", HttpStatus.CONFLICT),
    INVALID_WALLET_OPERATION(3207, "Invalid wallet operation", HttpStatus.BAD_REQUEST),
    WALLET_IS_INACTIVE(3208, "Wallet is inactive", HttpStatus.BAD_REQUEST),
    WALLET_DEDUCTION_FAILED(3209, "Wallet deduction failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // Reservation errors 29
    INVALID_RESERVATION_TIME(2910, "Invalid reservation time", HttpStatus.BAD_REQUEST),
    RESERVATION_NOT_FOUND(2911, "Reservation not found", HttpStatus.NOT_FOUND),
    RESERVATION_ALREADY_USED(2912, "This reservation has already been used", HttpStatus.BAD_REQUEST),
    RESERVATION_EXPIRED(2913, "Reservation has expired", HttpStatus.BAD_REQUEST),
    INVALID_RESERVATION_DATA(2914, "Invalid reservation", HttpStatus.BAD_REQUEST),
    RESERVATION_ALREADY_CANCELLED(2915, "Reservation has already been cancelled", HttpStatus.BAD_REQUEST),
    RESERVATION_CANNOT_BE_CANCELLED(2916, "Reservation cannot be cancelled in its current status", HttpStatus.BAD_REQUEST),
    RESERVATION_NOT_BELONG_TO_USER(2917, "Reservation does not belong to the user", HttpStatus.FORBIDDEN),
    MAX_ACTIVE_RESERVATIONS_EXCEEDED(2918, "Đã đạt giới hạn đặt chỗ ({0}). Vui lòng hoàn thành hoặc hủy đặt chỗ hiện tại trước khi tạo mới", HttpStatus.BAD_REQUEST),

    INVALID_USER_SUBSCRIPTION_TIME(21000, "Invalid _USER SUBSCRIPTION time", HttpStatus.BAD_REQUEST),
    USER_SUBSCRIPTION_NOT_FOUND(21001, "_USER SUBSCRIPTION not found", HttpStatus.NOT_FOUND),
    USER_SUBSCRIPTION_ALREADY_USED(21002, "This USER SUBSCRIPTION has already been used", HttpStatus.BAD_REQUEST),
    USER_SUBSCRIPTION_EXPIRED(21003, "USER SUBSCRIPTION has expired", HttpStatus.BAD_REQUEST),
    INVALID_USER_SUBSCRIPTION_DATA(21004, "Invalid USER SUBSCRIPTION", HttpStatus.BAD_REQUEST),
    ;


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
