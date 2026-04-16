package com.naminhyeok.fantazzk.auction;

import java.util.List;

public record AuctionRoomSetup(
    int teamCount,
    int teamSize,
    Integer budget,
    int pickBanTime,
    Integer minBidUnit,
    Integer positionLimit,
    List<AuctionPlayerSeed> players
) {
}
