package com.naminhyeok.fantazzk.room;

record RoomPlayerResponse(
    String name,
    String position,
    int displayOrder,
    String status
) {
    static RoomPlayerResponse from(RoomPlayer player) {
        return new RoomPlayerResponse(
            player.getName(),
            player.getPosition(),
            player.getDisplayOrder(),
            player.getStatus().name()
        );
    }
}
