package com.naminhyeok.fantazzk.room.web;

import com.naminhyeok.fantazzk.room.RoomView;
import java.util.List;

record RoomResponse(
    String code,
    String status,
    List<TeamLeaderResponse> teamLeaders
) {
    static RoomResponse from(RoomView room) {
        return new RoomResponse(
            room.code(),
            room.status(),
            room.teamLeaders().stream().map(TeamLeaderResponse::from).toList()
        );
    }
}
