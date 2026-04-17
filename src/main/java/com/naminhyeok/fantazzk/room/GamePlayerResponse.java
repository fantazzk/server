package com.naminhyeok.fantazzk.room;

record GamePlayerResponse(
    String name,
    String position,
    int displayOrder,
    String status
) {
    static GamePlayerResponse from(GamePlayer player, int displayOrder, boolean assigned) {
        return new GamePlayerResponse(
            player.name(),
            player.position(),
            displayOrder,
            assigned ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
        );
    }
}
