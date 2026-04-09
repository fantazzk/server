package com.naminhyeok.fantazzk.room;

import java.util.List;

record RoomProgressResponse(
    Integer currentTurnIndex,
    Integer currentRound,
    String currentLeaderId,
    List<String> currentRoundLeaderIds
) {
    static RoomProgressResponse from(Room room) {
        if (room.getStatus() != RoomStatus.IN_PROGRESS) {
            return new RoomProgressResponse(null, null, null, null);
        }

        if (room.getMode() == RoomMode.AUCTION) {
            return new RoomProgressResponse(null, room.getCurrentAuctionRound(), null, null);
        }

        DraftProgress progress = room.currentDraftProgress();
        return new RoomProgressResponse(
            room.getCurrentTurnIndex(),
            progress.currentRound(),
            progress.currentLeaderId(),
            progress.currentRoundLeaderIds()
        );
    }
}
