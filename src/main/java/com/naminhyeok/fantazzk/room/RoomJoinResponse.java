package com.naminhyeok.fantazzk.room;

record RoomJoinResponse(
    RoomDetailResponse room,
    TeamLeaderSessionResponse teamLeaderSession
) {
    static RoomJoinResponse from(RoomSessionResult result) {
        return new RoomJoinResponse(
            RoomDetailResponse.from(result.room()),
            TeamLeaderSessionResponse.from(result.room(), result.leader())
        );
    }
}
