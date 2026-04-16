package com.naminhyeok.fantazzk.auction;

public record AuctionBid(
    int round,
    int sequence,
    String leaderId,
    int amount
) {
}
