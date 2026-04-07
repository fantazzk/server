package com.naminhyeok.fantazzk.template;

import com.naminhyeok.fantazzk.ErrorDescriptor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.slf4j.event.Level;

@Getter
public enum TemplateErrorType implements ErrorDescriptor {
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEMPLATE_NOT_FOUND", "템플릿을 찾을 수 없습니다", Level.WARN),
    TEMPLATE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "TEMPLATE_INVALID_REQUEST", "템플릿 생성 요청이 올바르지 않습니다", Level.INFO);

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final Level logLevel;

    TemplateErrorType(HttpStatus status, String code, String message, Level logLevel) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }
}
