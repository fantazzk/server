package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.ErrorDescriptor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.slf4j.event.Level;

@Getter
enum RoomErrorType implements ErrorDescriptor {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "방을 찾을 수 없습니다", Level.WARN),
    ROOM_TEMPLATE_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "ROOM_TEMPLATE_NOT_FOUND",
        "방 생성에 사용할 템플릿을 찾을 수 없습니다",
        Level.WARN
    ),
    ROOM_JOIN_REQUIRES_WAITING(
        HttpStatus.CONFLICT,
        "ROOM_JOIN_REQUIRES_WAITING",
        "대기 중인 방에서만 참가할 수 있습니다",
        Level.INFO
    ),
    ROOM_FULL(HttpStatus.CONFLICT, "ROOM_FULL", "방이 가득 찼습니다", Level.INFO),
    ROOM_ACTION_TOKEN_REQUIRED(
        HttpStatus.UNAUTHORIZED,
        "ROOM_ACTION_TOKEN_REQUIRED",
        "방 액션 토큰이 필요합니다",
        Level.INFO
    ),
    ROOM_ACTION_TOKEN_INVALID(
        HttpStatus.UNAUTHORIZED,
        "ROOM_ACTION_TOKEN_INVALID",
        "유효한 방 액션 토큰이 아닙니다",
        Level.INFO
    ),
    ROOM_START_REQUIRES_WAITING(
        HttpStatus.CONFLICT,
        "ROOM_START_REQUIRES_WAITING",
        "대기 중인 방에서만 시작할 수 있습니다",
        Level.INFO
    ),
    ROOM_START_FORBIDDEN(
        HttpStatus.FORBIDDEN,
        "ROOM_START_FORBIDDEN",
        "방장은 방을 시작할 수 있는 유일한 팀장입니다",
        Level.INFO
    ),
    ROOM_LEADERS_NOT_FULL(
        HttpStatus.CONFLICT,
        "ROOM_LEADERS_NOT_FULL",
        "모든 팀장 자리가 채워져야 시작할 수 있습니다",
        Level.INFO
    );

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
