package com.naminhyeok.fantazzk.room;

public record GamePlayerView(
    String name,
    String position,
    int displayOrder,
    String status
) {
    static GamePlayerView from(GamePlayer player, int displayOrder, boolean assigned) {
        return new GamePlayerView(
            player.name(),
            player.position(),
            displayOrder,
            assigned ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
        );
    }
}
