package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ErrorDescriptor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

public enum RoomErrorType implements ErrorDescriptor {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "방을 찾을 수 없습니다", LogLevel.WARN),
    ROOM_TEMPLATE_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "ROOM_TEMPLATE_NOT_FOUND",
        "방 생성에 사용할 템플릿을 찾을 수 없습니다",
        LogLevel.WARN
    ),
    ROOM_NOT_JOINABLE(HttpStatus.CONFLICT, "ROOM_NOT_JOINABLE", "방에 참가할 수 없습니다", LogLevel.INFO),
    ROOM_FULL(HttpStatus.CONFLICT, "ROOM_FULL", "방이 가득 찼습니다", LogLevel.INFO),
    ROOM_NOT_STARTABLE(HttpStatus.CONFLICT, "ROOM_NOT_STARTABLE", "방을 시작할 수 없습니다", LogLevel.INFO);

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final LogLevel logLevel;

    RoomErrorType(HttpStatus status, String code, String message, LogLevel logLevel) {
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
