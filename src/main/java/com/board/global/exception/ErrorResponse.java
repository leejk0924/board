package com.board.global.exception;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public record ErrorResponse(int status, String message, String path, LocalDateTime timestamp) {

    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(errorCode.statusCode().value(), errorCode.getMessage(), path, LocalDateTime.now());
    }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(status.value(), message, path, LocalDateTime.now());
    }
}
