package com.naminhyeok.fantazzk.room;

import java.time.Instant;
import java.util.List;

record GameProgressResponse(
    Integer currentTurnIndex,
    Integer currentRound,
    String currentLeaderId,
    List<String> currentRoundLeaderIds,
    Instant currentAuctionRoundEndsAt,
    AuctionTargetResponse currentAuctionTarget,
    Integer highestBidAmount,
    String leadingLeaderId,
    Integer bidCount
) {
    static GameProgressResponse from(Game game) {
        if (game instanceof AuctionGame auctionGame) {
            if (auctionGame.getStatus() != GameStatus.IN_PROGRESS) {
                return empty();
            }
            AuctionBid winningBid = auctionGame.currentWinningBid();
            return new GameProgressResponse(
                null,
                auctionGame.getCurrentRound() <= 0 ? null : auctionGame.getCurrentRound(),
                null,
                null,
                auctionGame.getCurrentRoundEndsAt(),
                AuctionTargetResponse.from(auctionGame.currentAuctionTarget()),
                winningBid == null ? null : winningBid.amount(),
                winningBid == null ? null : winningBid.teamLeaderId().value(),
                auctionGame.currentBidCount()
            );
        }
        if (game instanceof DraftGame draftGame) {
            if (draftGame.getStatus() != GameStatus.IN_PROGRESS) {
                return empty();
            }
            DraftProgress progress = draftGame.currentDraftProgress();
            return new GameProgressResponse(
                draftGame.getCurrentTurnIndex(),
                progress.currentRound(),
                progress.currentLeaderId(),
                progress.currentRoundLeaderIds(),
                null,
                null,
                null,
                null,
                null
            );
        }
        return empty();
    }

    private static GameProgressResponse empty() {
        return new GameProgressResponse(null, null, null, null, null, null, null, null, null);
    }
}
