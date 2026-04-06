package com.naminhyeok.fantazzk.room.domain;

import org.springframework.lang.Nullable;

public final class AuctionRoundSettlement {
    private final String playerName;
    private final AuctionOutcome outcome;
    private final int nextRound;
    private final boolean completed;
    private final RoomBid winningBid;

    public AuctionRoundSettlement(
            String playerName,
            AuctionOutcome outcome,
            int nextRound,
            boolean completed,
            RoomBid winningBid
    ) {
        this.playerName = playerName;
        this.outcome = outcome;
        this.nextRound = nextRound;
        this.completed = completed;
        this.winningBid = winningBid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public AuctionOutcome getOutcome() {
        return outcome;
    }

    public int getNextRound() {
        return nextRound;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean getCompleted() {
        return completed;
    }

    @Nullable
    public RoomBid getWinningBid() {
        return winningBid;
    }
}
