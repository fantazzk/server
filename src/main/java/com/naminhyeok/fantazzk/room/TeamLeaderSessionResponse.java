package com.naminhyeok.fantazzk.room;

record TeamLeaderSessionResponse(
    String leaderId,
    String role,
    String actionToken
) {
    static TeamLeaderSessionResponse from(Room room, RoomTeamLeader leader) {
        return new TeamLeaderSessionResponse(
            leader.getTeamLeaderId(),
            room.getHostId().equals(leader.getTeamLeaderId()) ? "HOST" : "LEADER",
            leader.getActionToken()
        );
    }
}
