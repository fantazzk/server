package com.naminhyeok.fantazzk.room;

record AuctionSettlement(
    RoomPlayerId playerId,
    String playerName,
    AuctionOutcome outcome
) {
}
