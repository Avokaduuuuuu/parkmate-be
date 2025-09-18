package com.parkmate;

import com.parkmate.common.dto.ErrorResponse;
import com.parkmate.common.exception.BadRequestException;
import com.parkmate.common.exception.ConflictException;
import com.parkmate.common.exception.ForbiddenException;
import com.parkmate.common.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException ex) {
        logger.error("NotFoundException: {}", ex.getMessage());
        return new ErrorResponse("NOT_FOUND", ex.getMessage(), null);
    }

    // 400
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException ex) {
        logger.error("BadRequestException: {}", ex.getMessage());
        return new ErrorResponse("BAD_REQUEST", ex.getMessage(), null);
    }

    // 409 - Trùng
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(ConflictException ex) {
        logger.error("ConflictException: {}", ex.getMessage());
        return new ErrorResponse("CONFLICT", ex.getMessage(), null);
    }

    // 403
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleIllegalAccessException(IllegalAccessException ex) {
        logger.error("IllegalAccessException: {}", ex.getMessage());
        return new ErrorResponse("FORBIDDEN", ex.getMessage(), null);
    }

    // 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logger.error("MethodArgumentNotValidException: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        String errorMessage = "Invalid request body";
        return new ErrorResponse("VALIDATION_ERROR", errorMessage, details);
    }

    // 500
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnknown(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        logger.error("errorId={} - Unexpected error", errorId, ex);
        return new ErrorResponse("INTERNAL_ERROR",
                "Unexpected error occurred. Ref: " + errorId, null);
    }
}
