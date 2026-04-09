package com.naminhyeok.fantazzk.room;
import java.util.List;

record RoomResponse(
    String code,
    String status,
    String mode,
    int teamCount,
    String startReadiness,
    List<TeamLeaderResponse> teamLeaders
) {
    static RoomResponse from(Room room) {
        return new RoomResponse(
            room.getCode(),
            room.getStatus().name(),
            room.getMode().name(),
            room.getTeamCount(),
            room.getStartReadiness().name(),
            room.getLeaders().stream().map(TeamLeaderResponse::from).toList()
        );
    }
}
