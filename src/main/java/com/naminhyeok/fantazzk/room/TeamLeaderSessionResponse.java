package com.naminhyeok.fantazzk.room;

record TeamLeaderSessionResponse(
    String leaderId,
    String role,
    String actionToken
) {
    static TeamLeaderSessionResponse from(Room room, RoomTeamLeader leader) {
        return new TeamLeaderSessionResponse(
            leader.getId().value(),
            room.getHostLeaderId().equals(leader.getId()) ? "HOST" : "LEADER",
            leader.getActionToken()
        );
    }
}
