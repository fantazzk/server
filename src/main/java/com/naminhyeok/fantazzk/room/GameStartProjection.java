package com.naminhyeok.fantazzk.room;

record GameStartProjection(
    String mode,
    String status
) {
    static GameStartProjection from(Game game) {
        return new GameStartProjection(
            game instanceof AuctionGame ? RoomMode.AUCTION.name() : RoomMode.DRAFT.name(),
            game.getStatus().name()
        );
    }
}
