package com.parkmate.client.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.common.dto.ApiResponse;
import com.parkmate.common.exception.AppException;
import com.parkmate.common.exception.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Custom Feign ErrorDecoder to handle errors from other microservices.
 * Decodes ApiResponse error format and maps to AppException with appropriate ErrorCode.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("Decoding error from Feign client. Method: {}, Status: {}", methodKey, response.status());

        try {
            // Try to parse the error response body as ApiResponse
            if (response.body() != null) {
                try (InputStream bodyStream = response.body().asInputStream()) {
                    ApiResponse<?> apiResponse = objectMapper.readValue(bodyStream, ApiResponse.class);

                    if (apiResponse.error() != null) {
                        return mapToAppException(apiResponse, response, methodKey);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Failed to decode error response from {}: {}", methodKey, e.getMessage());
        }

        // Fallback: Map HTTP status to generic ErrorCode
        return mapHttpStatusToException(response.status(), methodKey);
    }

    /**
     * Maps ApiResponse.ErrorDetails to AppException with corresponding ErrorCode
     */
    private Exception mapToAppException(ApiResponse<?> apiResponse, Response response, String methodKey) {
        String errorCodeStr = apiResponse.error().code();
        String errorMessage = apiResponse.error().message();

        log.warn("Service error from {}: code={}, message={}", methodKey, errorCodeStr, errorMessage);

        // Check if it's a ParkingLotService error (code 3xxx)
        if (methodKey.contains("ParkingLotClient")) {
            try {
                int code = Integer.parseInt(errorCodeStr);
                ParkingLotServiceErrorCode parkingErrorCode = findParkingLotErrorCode(code);
                if (parkingErrorCode != null) {
                    // Create a custom AppException that wraps the parking service error
                    return new AppException(ErrorCode.OTHER_CLIENT_ERROR, errorMessage);
                }
            } catch (NumberFormatException e) {
                log.debug("Error code is not numeric: {}", errorCodeStr);
            }
        }

        // Check if it's a PaymentService error (code 4xxx or 5xxx)
        if (methodKey.contains("PaymentClient")) {
            // For payment service errors, use OTHER_CLIENT_ERROR with the original message
            return new AppException(ErrorCode.OTHER_CLIENT_ERROR, errorMessage);
        }

        // Default: map to generic error
        return mapHttpStatusToException(response.status(), errorMessage);
    }

    /**
     * Find ParkingLotServiceErrorCode by code number
     */
    private ParkingLotServiceErrorCode findParkingLotErrorCode(int code) {
        for (ParkingLotServiceErrorCode errorCode : ParkingLotServiceErrorCode.values()) {
            if (errorCode.getCode() == code) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * Map HTTP status code to ErrorCode
     */
    private Exception mapHttpStatusToException(int status, String message) {
        HttpStatus httpStatus = HttpStatus.resolve(status);

        if (httpStatus == null) {
            return new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION, "Unknown HTTP status: " + status);
        }

        return switch (httpStatus) {
            case NOT_FOUND -> new AppException(ErrorCode.INVALID_REQUEST, "Resource not found: " + message);
            case BAD_REQUEST -> new AppException(ErrorCode.INVALID_REQUEST, message);
            case UNAUTHORIZED, FORBIDDEN -> new AppException(ErrorCode.UNAUTHENTICATED, message);
            case CONFLICT -> new AppException(ErrorCode.INVALID_REQUEST, message);
            default -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION,
                    String.format("Service error [%d]: %s", status, message));
        };
    }
}