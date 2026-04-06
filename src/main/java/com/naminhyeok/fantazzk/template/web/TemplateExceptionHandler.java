package com.naminhyeok.fantazzk.template.web;

import com.naminhyeok.fantazzk.template.exception.TemplateException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(basePackageClasses = {TemplateExceptionHandler.class})
public class TemplateExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(TemplateExceptionHandler.class);

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex,
        Object body,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        String errorCode = ex instanceof MethodArgumentNotValidException ? "VALIDATION_ERROR" : "REQUEST_ERROR";
        String reason = ex instanceof MethodArgumentNotValidException
            ? "요청 값이 올바르지 않습니다"
            : (ex.getMessage() == null ? "Bad request" : ex.getMessage());
        Map<String, List<String>> data = null;
        Map<String, Object> errorData = null;
        if (ex instanceof MethodArgumentNotValidException validationException) {
            data = validationException
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(
                    Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                            fieldError ->
                                fieldError.getDefaultMessage() == null ? "" : fieldError.getDefaultMessage(),
                            Collectors.toList()
                        )
                    )
                );
            errorData = data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        log.warn("{}: {}", errorCode, reason);
        return ResponseEntity.status(status).headers(headers).body(ApiResponse.error(status.value(), errorCode, reason, errorData));
    }

    @ExceptionHandler(TemplateException.class)
    public ResponseEntity<ApiResponse<Object>> handle(TemplateException ex) {
        HttpStatus status;
        LogLevel logLevel;
        if (ex instanceof TemplateException.TemplateNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            logLevel = LogLevel.WARN;
        } else {
            status = HttpStatus.CONFLICT;
            logLevel = LogLevel.WARN;
        }
        log(logLevel, "TemplateException", ex);
        return ResponseEntity
            .status(status)
            .body(
                ApiResponse.error(
                    status.value(),
                    ex.getErrorCode(),
                    ex.getMessage() == null ? "" : ex.getMessage(),
                    Collections.emptyMap()
                )
            );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.warn("BadRequest: {}", ex.getMessage());
        return ResponseEntity
            .status(status)
            .body(
                ApiResponse.error(
                    status.value(),
                    "BAD_REQUEST",
                    ex.getMessage() == null ? "Bad request" : ex.getMessage(),
                    Collections.emptyMap()
                )
            );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(IllegalStateException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        log.warn("IllegalState: {}", ex.getMessage());
        return ResponseEntity
            .status(status)
            .body(
                ApiResponse.error(
                    status.value(),
                    "INVALID_STATE",
                    ex.getMessage() == null ? "Invalid state" : ex.getMessage(),
                    Collections.emptyMap()
                )
            );
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoSuchElement(NoSuchElementException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        log.warn("NoSuchElement: {}", ex.getMessage());
        return ResponseEntity
            .status(status)
            .body(
                ApiResponse.error(
                    status.value(),
                    "NOT_FOUND",
                    ex.getMessage() == null ? "Not found" : ex.getMessage(),
                    Collections.emptyMap()
                )
            );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Unexpected: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(status)
            .body(
                ApiResponse.error(
                    status.value(),
                    "INTERNAL_ERROR",
                    "예기치 못한 에러가 발생했습니다",
                    Collections.emptyMap()
                )
            );
    }

    private void log(LogLevel level, String prefix, Exception ex) {
        switch (level) {
            case ERROR -> log.error("{}: {}", prefix, ex.getMessage(), ex);
            case WARN -> log.warn("{}: {}", prefix, ex.getMessage());
            default -> log.info("{}: {}", prefix, ex.getMessage());
        }
    }
}
