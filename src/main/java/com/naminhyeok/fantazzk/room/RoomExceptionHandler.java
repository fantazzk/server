package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ApiResponse;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = RoomApiController.class)
public class RoomExceptionHandler {
    @ExceptionHandler(RoomTemplateNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleTemplateNotFound(RoomTemplateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "TEMPLATE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ApiResponse<Void>> handleRoomNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "ROOM_NOT_FOUND", "방을 찾을 수 없습니다"));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(HttpStatus.CONFLICT.value(), "INVALID_STATE", ex.getMessage()));
    }
}
