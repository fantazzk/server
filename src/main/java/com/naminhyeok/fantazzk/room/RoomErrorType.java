package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ErrorDescriptor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.slf4j.event.Level;

@Getter
public enum RoomErrorType implements ErrorDescriptor {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "방을 찾을 수 없습니다", Level.WARN),
    ROOM_TEMPLATE_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "ROOM_TEMPLATE_NOT_FOUND",
        "방 생성에 사용할 템플릿을 찾을 수 없습니다",
        Level.WARN
    ),
    ROOM_NOT_JOINABLE(HttpStatus.CONFLICT, "ROOM_NOT_JOINABLE", "방에 참가할 수 없습니다", Level.INFO),
    ROOM_FULL(HttpStatus.CONFLICT, "ROOM_FULL", "방이 가득 찼습니다", Level.INFO),
    ROOM_NOT_STARTABLE(HttpStatus.CONFLICT, "ROOM_NOT_STARTABLE", "방을 시작할 수 없습니다", Level.INFO);

    private final HttpStatus status;
    private final String code;
    private final String message;
    private final Level logLevel;

    RoomErrorType(HttpStatus status, String code, String message, Level logLevel) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.logLevel = logLevel;
    }
}
