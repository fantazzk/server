package com.naminhyeok.fantazzk.room;
import java.util.List;

record RoomResponse(
    String code,
    String status,
    List<TeamLeaderResponse> teamLeaders
) {
    static RoomResponse from(Room room) {
        return new RoomResponse(
            room.getCode(),
            room.getStatus().name(),
            room.getLeaders().stream().map(TeamLeaderResponse::from).toList()
        );
    }
}
