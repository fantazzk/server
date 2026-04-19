package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.AuctionBid;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameStatus;
import java.time.Instant;

public record AuctionProgressResponse(
    int currentRound,
    Instant currentAuctionRoundEndsAt,
    AuctionTargetResponse currentAuctionTarget,
    Integer highestBidAmount,
    String leadingLeaderId,
    int bidCount
) {
    public static AuctionProgressResponse from(Game game) {
        if (!(game instanceof AuctionGame auctionGame) || auctionGame.getStatus() != GameStatus.IN_PROGRESS) {
            return null;
        }
        AuctionBid winningBid = auctionGame.currentWinningBid();
        return new AuctionProgressResponse(
            auctionGame.getCurrentRound(),
            auctionGame.getCurrentRoundEndsAt(),
            AuctionTargetResponse.from(auctionGame.currentAuctionTarget()),
            winningBid == null ? null : winningBid.amount(),
            winningBid == null ? null : winningBid.teamLeaderId().value(),
            auctionGame.currentBidCount()
        );
    }
}
