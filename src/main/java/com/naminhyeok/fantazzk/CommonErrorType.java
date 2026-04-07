package com.naminhyeok.fantazzk;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorType implements ErrorDescriptor {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "요청이 올바르지 않습니다", LogLevel.WARN),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "예기치 못한 오류가 발생했습니다", LogLevel.ERROR);

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final LogLevel logLevel;

    CommonErrorType(HttpStatus status, String code, String message, LogLevel logLevel) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }
}
