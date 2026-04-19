package com.naminhyeok.fantazzk.room;

import java.util.List;

record RoomMembershipProjection(
    String status,
    String mode,
    int teamCount,
    int joinedLeaderCount,
    int remainingSlotCount,
    String startReadiness,
    List<TeamLeaderResponse> leaders
) {
    static RoomMembershipProjection from(Room room) {
        List<TeamLeaderResponse> leaders = room.getLeaders().stream().map(TeamLeaderResponse::from).toList();
        int joinedLeaderCount = leaders.size();
        return new RoomMembershipProjection(
            room.getStatus().name(),
            room.getMode().name(),
            room.getTeamCount(),
            joinedLeaderCount,
            room.getTeamCount() - joinedLeaderCount,
            room.getStartReadiness().name(),
            leaders
        );
    }
}
