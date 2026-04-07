package com.naminhyeok.fantazzk;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CoreException.class)
    ResponseEntity<ApiResponse<Void>> handleCoreException(CoreException ex) {
        return respond(ex, ex.getError(), ex.getData());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String message = ex.getMessage();
        Object data = message == null ? null : Map.of("detail", message);
        return respond(ex, CommonErrorType.BAD_REQUEST, data);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        return respond(ex, CommonErrorType.INTERNAL_SERVER_ERROR, null);
    }

    private ResponseEntity<ApiResponse<Void>> respond(Exception ex, ErrorDescriptor error, Object data) {
        log(ex, error.logLevel());
        return ResponseEntity.status(error.status())
            .body(ApiResponse.error(error, data));
    }

    private void log(Exception ex, LogLevel level) {
        switch (level) {
            case TRACE -> log.trace("Handled exception", ex);
            case DEBUG -> log.debug("Handled exception", ex);
            case INFO -> log.info("Handled exception", ex);
            case WARN -> log.warn("Handled exception", ex);
            case ERROR, FATAL -> log.error("Handled exception", ex);
            case OFF -> {
            }
        }
    }

    private static final class LegacyErrorDescriptor implements ErrorDescriptor {
        private final HttpStatus status;
        private final String code;
        private final String message;

        private LegacyErrorDescriptor(HttpStatus status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
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
            return LogLevel.WARN;
        }
    }
}
