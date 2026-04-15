package com.naminhyeok.fantazzk.room;

import java.time.Instant;
import java.util.List;

record RoomProgressResponse(
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
    static RoomProgressResponse from(Room room) {
        return new RoomProgressResponse(null, null, null, null, null, null, null, null, null);
    }

    static RoomProgressResponse from(RoomDetails details) {
        if (details.game() instanceof AuctionGame auctionGame) {
            if (auctionGame.getStatus() != GameStatus.IN_PROGRESS) {
                return new RoomProgressResponse(null, null, null, null, null, null, null, null, null);
            }
            RoomBid winningBid = auctionGame.currentWinningBid();
            return new RoomProgressResponse(
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
        if (details.game() instanceof DraftGame draftGame) {
            if (draftGame.getStatus() != GameStatus.IN_PROGRESS) {
                return new RoomProgressResponse(null, null, null, null, null, null, null, null, null);
            }
            DraftProgress progress = draftGame.currentDraftProgress();
            return new RoomProgressResponse(
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
        return new RoomProgressResponse(null, null, null, null, null, null, null, null, null);
    }
}
