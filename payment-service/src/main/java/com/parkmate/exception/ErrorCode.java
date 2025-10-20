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
    IO_ERROR(1004, "I/O error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1005, "Unauthenticated", HttpStatus.UNAUTHORIZED),

    // Payment errors 31
    PAYMENT_NOT_FOUND(3101, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_EXISTS(3102, "Payment with ID '{0}' already exists", HttpStatus.CONFLICT),
    PAYMENT_METHOD_NOT_SUPPORTED(3103, "Payment method '{0}' is not supported", HttpStatus.BAD_REQUEST),
    PAYMENT_PROCESSING_FAILED(3104, "Payment processing failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INSUFFICIENT_FUNDS(3105, "Insufficient funds", HttpStatus.PAYMENT_REQUIRED),
    REFUND_NOT_ALLOWED(3106, "Refund not allowed for this payment", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND(3107, "Transaction not found", HttpStatus.NOT_FOUND),
    TRANSACTION_ALREADY_EXISTS(3108, "Transaction with ID '{0}' already exists", HttpStatus.CONFLICT),
    TRANSACTION_FAILED(3109, "Transaction failed", HttpStatus.INTERNAL_SERVER_ERROR),
    CURRENCY_NOT_SUPPORTED(3110, "Currency '{0}' is not supported", HttpStatus.BAD_REQUEST),
    EXCHANGE_RATE_NOT_FOUND(3111, "Exchange rate not found for currency pair '{0}' to '{1}'", HttpStatus.NOT_FOUND),
    LIMIT_EXCEEDED(3112, "Transaction limit exceeded", HttpStatus.BAD_REQUEST),
    FRAUD_DETECTED(3113, "Potential fraud detected", HttpStatus.FORBIDDEN),
    PAYMENT_GATEWAY_UNAVAILABLE(3114, "Payment gateway is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_PAYMENT_DETAILS(3115, "Invalid payment details provided", HttpStatus.BAD_REQUEST),

    //WALLET errors 32
    WALLET_NOT_FOUND(3201, "Wallet not found", HttpStatus.NOT_FOUND),
    WALLET_ALREADY_EXISTS(3202, "Wallet with ID '{0}' already exists", HttpStatus.CONFLICT),
    INSUFFICIENT_WALLET_BALANCE(3203, "Insufficient wallet balance", HttpStatus.BAD_REQUEST),
    WALLET_TOPUP_FAILED(3204, "Wallet top-up failed", HttpStatus.INTERNAL_SERVER_ERROR),
    WALLET_TRANSACTION_NOT_FOUND(3205, "Wallet transaction not found", HttpStatus.NOT_FOUND),
    WALLET_TRANSACTION_ALREADY_EXISTS(3206, "Wallet transaction with ID '{0}' already exists", HttpStatus.CONFLICT),
    INVALID_WALLET_OPERATION(3207, "Invalid wallet operation", HttpStatus.BAD_REQUEST),
    WALLET_IS_INACTIVE(3208, "Wallet is inactive", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(3209, "User with ID '{0}' not found", HttpStatus.NOT_FOUND),
    INVALID_TRANSACTION_TYPE(3210, "Invalid transaction type '{0}'", HttpStatus.BAD_REQUEST),
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
