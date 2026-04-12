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
        if (room.getStatus() != RoomStatus.IN_PROGRESS) {
            return new RoomProgressResponse(null, null, null, null, null, null, null, null, null);
        }

        if (room.getMode() == RoomMode.AUCTION) {
            RoomBid winningBid = room.currentWinningBid();
            return new RoomProgressResponse(
                null,
                room.getCurrentAuctionRound(),
                null,
                null,
                room.getCurrentAuctionRoundEndsAt(),
                AuctionTargetResponse.from(room.currentAuctionTarget()),
                winningBid == null ? null : winningBid.amount(),
                winningBid == null ? null : winningBid.teamLeaderId().value(),
                room.currentBidCount()
            );
        }

        DraftProgress progress = room.currentDraftProgress();
        return new RoomProgressResponse(
            room.getCurrentTurnIndex(),
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
}
