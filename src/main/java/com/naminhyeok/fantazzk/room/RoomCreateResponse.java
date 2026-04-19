package com.naminhyeok.fantazzk.room;

record RoomCreateResponse(
    RoomDetailResponse room,
    TeamLeaderSessionResponse teamLeaderSession
) {
    static RoomCreateResponse from(RoomSessionResult result) {
        return new RoomCreateResponse(
            RoomDetailResponse.from(result.room()),
            TeamLeaderSessionResponse.from(result.room(), result.leader())
        );
    }
}
