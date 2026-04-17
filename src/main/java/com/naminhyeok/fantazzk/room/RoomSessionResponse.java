package com.naminhyeok.fantazzk.room;

record RoomSessionResponse(
    RoomViewResponse room,
    TeamLeaderSessionResponse teamLeaderSession
) {
    static RoomSessionResponse from(RoomSessionResult result) {
        return from(result.room(), result.leader());
    }

    static RoomSessionResponse fromHost(RoomDetails details) {
        Room room = details.room();
        RoomTeamLeader host = room.getLeaders().stream()
            .filter(leader -> leader.getId().equals(room.getHostLeaderId()))
            .findFirst()
            .orElseThrow();
        return from(details, host);
    }

    static RoomSessionResponse fromHost(Room room) {
        return fromHost(RoomDetails.from(room));
    }

    static RoomSessionResponse from(RoomDetails details, RoomTeamLeader leader) {
        return new RoomSessionResponse(
            RoomViewResponse.from(details.room()),
            TeamLeaderSessionResponse.from(details.room(), leader)
        );
    }

    static RoomSessionResponse from(Room room, RoomTeamLeader leader) {
        return from(RoomDetails.from(room), leader);
    }
}
