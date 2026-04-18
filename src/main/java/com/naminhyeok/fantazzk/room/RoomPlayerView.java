package com.naminhyeok.fantazzk.room;

public record RoomPlayerView(
    String name,
    String position,
    int displayOrder,
    String status
) {
    static RoomPlayerView from(RoomPlayer player) {
        return new RoomPlayerView(
            player.getName(),
            player.getPosition(),
            player.getDisplayOrder(),
            player.getStatus().name()
        );
    }

    static RoomPlayerView from(RoomPlayer player, int displayOrder, boolean assigned) {
        return new RoomPlayerView(
            player.getName(),
            player.getPosition(),
            displayOrder,
            assigned ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
        );
    }
}
