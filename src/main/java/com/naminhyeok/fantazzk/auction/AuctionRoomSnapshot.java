package com.naminhyeok.fantazzk.auction;

import java.time.Instant;
import java.util.List;

record AuctionRoomSnapshot(
    String code,
    Instant createdAt,
    String hostLeaderId,
    int teamCount,
    int teamSize,
    Integer budget,
    int pickBanTime,
    Integer minBidUnit,
    Integer positionLimit,
    AuctionRoomStatus status,
    Integer currentAuctionRound,
    Instant currentAuctionRoundEndsAt,
    List<Leader> leaders,
    List<Player> players,
    List<Member> members,
    List<Bid> bids
) {
    record Leader(String leaderId, String nickname, Integer remainingBudget) {
    }

    record Player(int playerId, String name, String position, int displayOrder, AuctionPlayerStatus status) {
    }

    record Member(String leaderId, int playerId, int sequence) {
    }

    record Bid(int round, int sequence, String leaderId, int amount) {
    }
}
