package com.naminhyeok.fantazzk.auction;

public record AuctionSettlement(
    int playerId,
    String playerName,
    AuctionOutcome outcome
) {
}
