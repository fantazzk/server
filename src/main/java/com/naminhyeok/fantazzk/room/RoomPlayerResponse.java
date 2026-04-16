package com.naminhyeok.fantazzk.room;

record RoomPlayerResponse(
    int playerId,
    String name,
    String position,
    int displayOrder,
    String status
) {
    static RoomPlayerResponse from(RoomPlayer player) {
        return new RoomPlayerResponse(
            player.getId().value(),
            player.getName(),
            player.getPosition(),
            player.getDisplayOrder(),
            player.getStatus().name()
        );
    }
}
