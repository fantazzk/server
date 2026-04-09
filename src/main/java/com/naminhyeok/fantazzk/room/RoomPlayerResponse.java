package com.naminhyeok.fantazzk.room;

record RoomPlayerResponse(
    String name,
    int displayOrder,
    String status
) {
    static RoomPlayerResponse from(RoomPlayer player) {
        return new RoomPlayerResponse(
            player.getName(),
            player.getDisplayOrder(),
            player.getStatus().name()
        );
    }
}
