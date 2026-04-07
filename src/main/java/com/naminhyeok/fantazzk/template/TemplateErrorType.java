package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.ErrorDescriptor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

public enum TemplateErrorType implements ErrorDescriptor {
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND", "템플릿을 찾을 수 없습니다", LogLevel.WARN),
    TEMPLATE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "TEMPLATE_INVALID_REQUEST", "템플릿 생성 요청이 올바르지 않습니다", LogLevel.INFO);

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final LogLevel logLevel;

    TemplateErrorType(HttpStatus status, String code, String message, LogLevel logLevel) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public LogLevel logLevel() {
        return logLevel;
    }
}
