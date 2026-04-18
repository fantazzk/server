package com.naminhyeok.fantazzk.room;

import java.time.Instant;
import java.util.List;

public record GameProgressView(
    Integer currentTurnIndex,
    Integer currentRound,
    String currentLeaderId,
    List<String> currentRoundLeaderIds,
    Instant currentAuctionRoundEndsAt,
    AuctionTargetView currentAuctionTarget,
    Integer highestBidAmount,
    String leadingLeaderId,
    Integer bidCount
) {
    static GameProgressView from(Game game) {
        if (game instanceof AuctionGame auctionGame) {
            if (auctionGame.getStatus() != GameStatus.IN_PROGRESS) {
                return empty();
            }
            AuctionBid winningBid = auctionGame.currentWinningBid();
            return new GameProgressView(
                null,
                auctionGame.getCurrentRound() <= 0 ? null : auctionGame.getCurrentRound(),
                null,
                null,
                auctionGame.getCurrentRoundEndsAt(),
                AuctionTargetView.from(auctionGame.currentAuctionTarget()),
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
            return new GameProgressView(
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

    private static GameProgressView empty() {
        return new GameProgressView(null, null, null, null, null, null, null, null, null);
    }
}
