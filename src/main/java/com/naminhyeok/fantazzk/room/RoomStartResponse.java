package com.naminhyeok.fantazzk.room;

record RoomStartResponse(
    String gameId,
    String roomCode,
    String mode,
    String status
) {
    static RoomStartResponse from(Game game) {
        return new RoomStartResponse(
            game.getId().gameId().toString(),
            game.getRoomCode(),
            game instanceof AuctionGame ? RoomMode.AUCTION.name() : RoomMode.DRAFT.name(),
            game.getStatus().name()
        );
    }
}
