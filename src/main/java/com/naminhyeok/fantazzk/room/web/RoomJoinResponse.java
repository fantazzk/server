package com.naminhyeok.fantazzk.room.web;

import com.naminhyeok.fantazzk.room.application.RoomSessionResult;
import com.naminhyeok.fantazzk.room.query.RoomDetailResponse;

public record RoomJoinResponse(
    RoomDetailResponse room,
    TeamLeaderSessionResponse teamLeaderSession
) {
    public static RoomJoinResponse from(RoomSessionResult result) {
        return new RoomJoinResponse(
            RoomDetailResponse.from(result.room()),
            TeamLeaderSessionResponse.from(result.room(), result.leader())
        );
    }
}
