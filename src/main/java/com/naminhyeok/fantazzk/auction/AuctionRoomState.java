package com.naminhyeok.fantazzk.auction;

import java.time.Instant;
import java.util.List;

public record AuctionRoomState(
    String code,
    AuctionRoomStatus status,
    List<Leader> leaders,
    List<Player> players,
    List<Member> members,
    Integer currentRound,
    Instant currentRoundEndsAt,
    AuctionTarget currentTarget,
    Integer highestBidAmount,
    String leadingLeaderId,
    int bidCount
) {
    public record Leader(
        String leaderId,
        String nickname,
        Integer remainingBudget
    ) {
    }

    public record Player(
        int playerId,
        String name,
        String position,
        int displayOrder,
        boolean assigned
    ) {
    }

    public record Member(
        String leaderId,
        int playerId,
        int assignOrder
    ) {
    }
}
