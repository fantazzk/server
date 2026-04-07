package com.naminhyeok.fantazzk;

import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.slf4j.event.Level;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CoreException.class)
    ResponseEntity<ApiResponse<Void>> handleCoreException(CoreException ex) {
        return respond(ex, ex.getError(), ex.getData());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        LinkedHashMap<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.status(CommonErrorType.BAD_REQUEST.getStatus())
            .body(ApiResponse.error(CommonErrorType.BAD_REQUEST, errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return respond(ex, CommonErrorType.BAD_REQUEST, null);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return respond(ex, CommonErrorType.INTERNAL_SERVER_ERROR, null);
    }

    private ResponseEntity<ApiResponse<Void>> respond(Exception ex, ErrorDescriptor error, Object data) {
        log(ex, error.getLogLevel());
        return ResponseEntity.status(error.getStatus())
            .body(ApiResponse.error(error, data));
    }

    private void log(Exception ex, Level level) {
        switch (level) {
            case TRACE -> log.trace("Handled exception", ex);
            case DEBUG -> log.debug("Handled exception", ex);
            case INFO -> log.info("Handled exception", ex);
            case WARN -> log.warn("Handled exception", ex);
            case ERROR -> log.error("Handled exception", ex);
        }
    }
}
