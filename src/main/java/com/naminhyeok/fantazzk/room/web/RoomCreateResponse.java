package com.naminhyeok.fantazzk.room.web;

import com.naminhyeok.fantazzk.room.application.RoomSessionResult;
import com.naminhyeok.fantazzk.room.query.RoomDetailResponse;

public record RoomCreateResponse(
    RoomDetailResponse room,
    TeamLeaderSessionResponse teamLeaderSession
) {
    public static RoomCreateResponse from(RoomSessionResult result) {
        return new RoomCreateResponse(
            RoomDetailResponse.from(result.room()),
            TeamLeaderSessionResponse.from(result.room(), result.leader())
        );
    }
}
