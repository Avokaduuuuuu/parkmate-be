package com.parkmate.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // General errors
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1001, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1002, "Invalid request", HttpStatus.BAD_REQUEST),

    // ParkingLot errors
    PARKING_NOT_FOUND(1003, "Parking Lot not found", HttpStatus.NOT_FOUND),
    INVALID_PARKING_LOT_STATUS_TRANSITION(1004, "PENDING Parking Lot can not be deleted", HttpStatus.CONFLICT);

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
