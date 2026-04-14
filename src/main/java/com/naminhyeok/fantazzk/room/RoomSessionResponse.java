package com.naminhyeok.fantazzk.room;

record RoomSessionResponse(
    RoomResponse room,
    TeamLeaderSessionResponse teamLeaderSession
) {
    static RoomSessionResponse from(Room room, RoomTeamLeader leader) {
        return new RoomSessionResponse(
            RoomResponse.from(room),
            TeamLeaderSessionResponse.from(room, leader)
        );
    }

    static RoomSessionResponse from(RoomSessionResult result) {
        return from(result.room(), result.leader());
    }
}
