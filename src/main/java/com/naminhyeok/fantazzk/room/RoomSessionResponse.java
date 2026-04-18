package com.naminhyeok.fantazzk.room;

record RoomSessionResponse(
    RoomViewResponse room,
    TeamLeaderSessionResponse teamLeaderSession
) {
    static RoomSessionResponse from(RoomSessionResult result) {
        return from(result.room(), result.leader());
    }

    static RoomSessionResponse fromHost(Room room) {
        RoomTeamLeader host = room.getLeaders().stream()
            .filter(leader -> leader.getId().equals(room.getHostLeaderId()))
            .findFirst()
            .orElseThrow();
        return from(room, host);
    }

    static RoomSessionResponse from(Room room, RoomTeamLeader leader) {
        return new RoomSessionResponse(
            RoomViewResponse.from(room),
            TeamLeaderSessionResponse.from(room, leader)
        );
    }
}
