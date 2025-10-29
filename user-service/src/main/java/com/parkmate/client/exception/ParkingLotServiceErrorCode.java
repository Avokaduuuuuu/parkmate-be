package com.parkmate.client.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ParkingLotServiceErrorCode {

    // General errors
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(1001, "Validation failed", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST(1002, "Invalid request", HttpStatus.BAD_REQUEST),
    INVALID_ENUM(1003, "Invalid enum", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1004, "Unauthorized", HttpStatus.UNAUTHORIZED),
    VEHICLE_TYPE_MISMATCH(1005, "Vehicle type mismatch", HttpStatus.BAD_REQUEST),
    UNABLE_TO_DELETE_MAP(1006, "Unable to delete map", HttpStatus.CONFLICT),

    // ParkingLot errors
    PARKING_NOT_FOUND(3101, "Parking Lot not found", HttpStatus.NOT_FOUND),
    INVALID_PARKING_LOT_STATUS_TRANSITION(3102, "PENDING Parking Lot can not be deleted", HttpStatus.CONFLICT),
    REASON_REQUIRED(3103, "Reason required", HttpStatus.BAD_REQUEST),

    // ParkingFloor errors
    PARKING_FLOOR_NOT_FOUND(3201, "Parking Floor not found", HttpStatus.NOT_FOUND),
    INVALID_PARKING_FLOOR_STATUS_TRANSITION(3202, "Parking Floor is disable", HttpStatus.CONFLICT),
    INACTIVE_FLOOR(3203, "Parking Floor is inactive", HttpStatus.BAD_REQUEST),


    // ParkingArea errors
    PARKING_AREA_NOT_FOUND(3301, "Parking Area not found", HttpStatus.NOT_FOUND),

    // PricingRule errors
    PRICING_RULE_NOT_FOUND(3401, "Pricing Rule not found", HttpStatus.NOT_FOUND),
    INVALID_RULE_SCOPE(3402, "Invalid rule scope", HttpStatus.BAD_REQUEST),
    PRICING_RULE_LOT_MISMATCH(3403, "Pricing Rule lot mismatch", HttpStatus.BAD_REQUEST),

    // Spot errors
    SPOT_NOT_FOUND(3501, "Spot not found", HttpStatus.NOT_FOUND),
    SPOT_COUNT_MISS_MATCH(3502, "Spot count mismatch", HttpStatus.CONFLICT),
    VEHICLE_TYPE_MISS_MATCH(3503, "Spot can not be added to this area", HttpStatus.CONFLICT),
    BLOCK_REASON_REQUIRED(3504, "Block Reason required", HttpStatus.BAD_REQUEST),
    SPOT_RELEASED_FAILED(3505, "Fail to release session for this spot", HttpStatus.CONFLICT),
    SPOT_HELD_FAILED(3506, "Fail to create session for this spot", HttpStatus.CONFLICT),

    // Session errors
    SESSION_NOT_FOUND(3601, "Session Not found", HttpStatus.NOT_FOUND),

    // Image errors
    INVALID_IMAGE(3602, "Invalid image", HttpStatus.BAD_REQUEST),

    // Subscription errors
    SUBSCRIPTION_NOT_FOUND(3701, "Subscription not found", HttpStatus.NOT_FOUND),;

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ParkingLotServiceErrorCode(int code, String message, HttpStatus httpStatus) {
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
