package com.naminhyeok.fantazzk.room;

import java.util.List;

public record RoomResponse(
    String code,
    RoomStatus status,
    List<TeamLeaderResponse> teamLeaders
) {
    static RoomResponse from(Room room) {
        return new RoomResponse(
            room.getCode(),
            room.getStatus(),
            room.getLeaders().stream().map(TeamLeaderResponse::from).toList()
        );
    }
}
