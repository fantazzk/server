package com.naminhyeok.fantazzk.room;

public record TeamLeaderSessionView(
    String leaderId,
    String role,
    String actionToken
) {
    static TeamLeaderSessionView from(Room room, RoomTeamLeader leader) {
        return new TeamLeaderSessionView(
            leader.getId().value(),
            room.getHostLeaderId().equals(leader.getId()) ? "HOST" : "LEADER",
            leader.getActionToken()
        );
    }
}
