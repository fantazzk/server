package com.naminhyeok.fantazzk.room;

public record RoomSessionView(
    RoomView room,
    TeamLeaderSessionView teamLeaderSession
) {
    static RoomSessionView from(RoomSessionResult result) {
        return from(result.room(), result.leader());
    }

    static RoomSessionView fromHost(Room room) {
        RoomTeamLeader host = room.getLeaders().stream()
            .filter(leader -> leader.getId().equals(room.getHostLeaderId()))
            .findFirst()
            .orElseThrow();
        return from(room, host);
    }

    static RoomSessionView from(Room room, RoomTeamLeader leader) {
        return new RoomSessionView(
            RoomView.from(room),
            TeamLeaderSessionView.from(room, leader)
        );
    }
}
