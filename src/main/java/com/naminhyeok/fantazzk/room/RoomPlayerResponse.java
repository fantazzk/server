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

    static RoomPlayerResponse from(RoomPlayer player, int displayOrder, boolean assigned) {
        return new RoomPlayerResponse(
            player.getName(),
            player.getPosition(),
            displayOrder,
            assigned ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
        );
    }
}
