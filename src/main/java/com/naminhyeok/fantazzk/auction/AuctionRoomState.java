package com.naminhyeok.fantazzk.auction;

import java.time.Instant;

public record AuctionRoomState(
    String code,
    AuctionRoomStatus status,
    Integer currentRound,
    Instant currentRoundEndsAt,
    AuctionTarget currentTarget,
    Integer highestBidAmount,
    int bidCount
) {
}
