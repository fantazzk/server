package com.naminhyeok.fantazzk.room;

record RoomSessionResponse(
    RoomResponse room,
    TeamLeaderSessionResponse teamLeaderSession
) {
    static RoomSessionResponse fromHost(Room room) {
        RoomTeamLeader host = room.getLeaders().stream()
            .filter(leader -> leader.getId().equals(room.getHostLeaderId()))
            .findFirst()
            .orElseThrow();
        return from(room, host);
    }

    static RoomSessionResponse from(Room room, RoomTeamLeader leader) {
        return new RoomSessionResponse(
            RoomResponse.from(room),
            TeamLeaderSessionResponse.from(room, leader)
        );
    }
}
