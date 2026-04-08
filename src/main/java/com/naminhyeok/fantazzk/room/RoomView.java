package com.naminhyeok.fantazzk.room;

import java.util.List;

public record RoomView(
    String code,
    String status,
    List<TeamLeaderView> teamLeaders
) {
    static RoomView from(Room room) {
        return new RoomView(
            room.getCode(),
            room.getStatus().name(),
            room.getLeaders().stream().map(TeamLeaderView::from).toList()
        );
    }
}
