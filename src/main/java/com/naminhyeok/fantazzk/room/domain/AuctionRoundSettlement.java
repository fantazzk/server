package com.naminhyeok.fantazzk.room.domain;

public record AuctionRoundSettlement(
    String playerName,
    AuctionOutcome outcome,
    int nextRound,
    boolean completed,
    RoomBid winningBid
) {
}
