package com.parkmate.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] params;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.params = new Object[0];
    }

    public AppException(ErrorCode errorCode, Object... params) {
        super(errorCode.formatMessage(params));
        this.errorCode = errorCode;
        this.params = params;
    }

    public int getCode() {
        return errorCode.getCode();
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }
}