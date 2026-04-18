package com.naminhyeok.fantazzk.room.domain.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.ErrorDescriptor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.slf4j.event.Level;

@Getter
public enum RoomErrorType implements ErrorDescriptor {
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "방을 찾을 수 없습니다", Level.WARN),
    GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "GAME_NOT_FOUND", "게임을 찾을 수 없습니다", Level.WARN),
    ROOM_TEMPLATE_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "ROOM_TEMPLATE_NOT_FOUND",
        "방 생성에 사용할 템플릿을 찾을 수 없습니다",
        Level.WARN
    ),
    ROOM_CODE_GENERATION_FAILED(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "ROOM_CODE_GENERATION_FAILED",
        "방 코드를 생성하지 못했습니다",
        Level.ERROR
    ),
    ROOM_STATE_INVALID(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "ROOM_STATE_INVALID",
        "방 상태가 올바르지 않습니다. 잠시 후 다시 시도해 주세요",
        Level.ERROR
    ),
    ROOM_JOIN_REQUIRES_WAITING(
        HttpStatus.CONFLICT,
        "ROOM_JOIN_REQUIRES_WAITING",
        "대기 중인 방에서만 참가할 수 있습니다",
        Level.INFO
    ),
    ROOM_FULL(HttpStatus.CONFLICT, "ROOM_FULL", "방이 가득 찼습니다", Level.INFO),
    ROOM_NICKNAME_ALREADY_TAKEN(
        HttpStatus.CONFLICT,
        "ROOM_NICKNAME_ALREADY_TAKEN",
        "이미 사용 중인 닉네임입니다",
        Level.INFO
    ),
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
    ROOM_DRAFT_POSITION_REQUIRES_WAITING(
        HttpStatus.CONFLICT,
        "ROOM_DRAFT_POSITION_REQUIRES_WAITING",
        "드래프트 자리는 대기 중인 방에서만 선택할 수 있습니다",
        Level.INFO
    ),
    ROOM_DRAFT_POSITION_REQUIRES_DRAFT_MODE(
        HttpStatus.CONFLICT,
        "ROOM_DRAFT_POSITION_REQUIRES_DRAFT_MODE",
        "드래프트 방에서만 드래프트 자리를 선택할 수 있습니다",
        Level.INFO
    ),
    ROOM_DRAFT_POSITION_OUT_OF_RANGE(
        HttpStatus.BAD_REQUEST,
        "ROOM_DRAFT_POSITION_OUT_OF_RANGE",
        "유효한 드래프트 자리가 아닙니다",
        Level.INFO
    ),
    ROOM_DRAFT_POSITION_TAKEN(
        HttpStatus.CONFLICT,
        "ROOM_DRAFT_POSITION_TAKEN",
        "이미 선택된 드래프트 자리입니다",
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
    ROOM_PLAY_REQUIRES_IN_PROGRESS(
        HttpStatus.CONFLICT,
        "ROOM_PLAY_REQUIRES_IN_PROGRESS",
        "진행 중인 방에서만 플레이할 수 있습니다",
        Level.INFO
    ),
    ROOM_BID_REQUIRES_AUCTION_MODE(
        HttpStatus.CONFLICT,
        "ROOM_BID_REQUIRES_AUCTION_MODE",
        "경매 방에서만 입찰 또는 정산할 수 있습니다",
        Level.INFO
    ),
    ROOM_BID_AMOUNT_NOT_POSITIVE(
        HttpStatus.BAD_REQUEST,
        "ROOM_BID_AMOUNT_NOT_POSITIVE",
        "입찰 금액은 0보다 커야 합니다",
        Level.INFO
    ),
    ROOM_BIDDER_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "ROOM_BIDDER_NOT_FOUND",
        "입찰할 팀장을 찾을 수 없습니다",
        Level.INFO
    ),
    ROOM_BID_BUDGET_EXCEEDED(
        HttpStatus.BAD_REQUEST,
        "ROOM_BID_BUDGET_EXCEEDED",
        "남은 예산보다 큰 금액은 입찰할 수 없습니다",
        Level.INFO
    ),
    ROOM_BID_REQUIRES_OPEN_ROUND(
        HttpStatus.CONFLICT,
        "ROOM_BID_REQUIRES_OPEN_ROUND",
        "열린 경매 라운드에서만 입찰할 수 있습니다",
        Level.INFO
    ),
    ROOM_BID_TOO_LOW(
        HttpStatus.CONFLICT,
        "ROOM_BID_TOO_LOW",
        "현재 최고가보다 높은 금액만 입찰할 수 있습니다",
        Level.INFO
    ),
    ROOM_BID_MIN_UNIT_NOT_MET(
        HttpStatus.CONFLICT,
        "ROOM_BID_MIN_UNIT_NOT_MET",
        "최소 입찰 증가폭을 만족하는 금액만 입찰할 수 있습니다",
        Level.INFO
    ),
    ROOM_AUCTION_POSITION_LIMIT_EXCEEDED(
        HttpStatus.CONFLICT,
        "ROOM_AUCTION_POSITION_LIMIT_EXCEEDED",
        "같은 포지션 선수는 팀당 제한 인원까지만 배정할 수 있습니다",
        Level.INFO
    ),
    ROOM_AUCTION_ROUND_NOT_ENDED(
        HttpStatus.CONFLICT,
        "ROOM_AUCTION_ROUND_NOT_ENDED",
        "경매 라운드가 아직 종료되지 않았습니다",
        Level.INFO
    ),
    ROOM_PICK_REQUIRES_DRAFT_MODE(
        HttpStatus.CONFLICT,
        "ROOM_PICK_REQUIRES_DRAFT_MODE",
        "드래프트 방에서만 픽할 수 있습니다",
        Level.INFO
    ),
    ROOM_PICK_OUT_OF_TURN(
        HttpStatus.CONFLICT,
        "ROOM_PICK_OUT_OF_TURN",
        "현재 턴인 팀장만 픽할 수 있습니다",
        Level.INFO
    ),
    ROOM_PICK_PLAYER_NOT_AVAILABLE(
        HttpStatus.CONFLICT,
        "ROOM_PICK_PLAYER_NOT_AVAILABLE",
        "현재 픽 가능한 선수를 찾을 수 없습니다",
        Level.INFO
    ),
    ROOM_LEADERS_NOT_FULL(
        HttpStatus.CONFLICT,
        "ROOM_LEADERS_NOT_FULL",
        "모든 팀장 자리가 채워져야 시작할 수 있습니다",
        Level.INFO
    ),
    ROOM_DRAFT_POSITIONS_NOT_FULL(
        HttpStatus.CONFLICT,
        "ROOM_DRAFT_POSITIONS_NOT_FULL",
        "모든 팀장이 서로 다른 드래프트 자리를 확정해야 시작할 수 있습니다",
        Level.INFO
    ),
    ROOM_CONCURRENT_MODIFICATION(
        HttpStatus.CONFLICT,
        "ROOM_CONCURRENT_MODIFICATION",
        "방 상태가 동시에 변경되었습니다. 최신 상태를 확인한 뒤 다시 시도해 주세요",
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
