package com.naminhyeok.fantazzk.room.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "현재 사용자의 방 역할")
public enum TeamLeaderRole {
    HOST,
    LEADER
}
