package com.naminhyeok.fantazzk.room.domain;

public record AuctionRound(int round, RoomBid highestBid) {
    public AuctionRound(int round) {
        this(round, null);
    }

    public AuctionRound {
        if (round <= 0) {
            throw new IllegalArgumentException("경매 라운드는 1 이상이어야 합니다");
        }
    }

    public void requireHigherBid(int amount) {
        if (amount <= highestAmount()) {
            throw new IllegalArgumentException("현재 최고가보다 높아야 합니다");
        }
    }

    public void requireRosterCapacity(
        int currentMemberCount,
        int picksPerTeam
    ) {
        if (currentMemberCount >= picksPerTeam) {
            throw new IllegalStateException("팀장의 팀원 정원이 가득 찼습니다");
        }
    }

    public AuctionRoundSettlement settle(
        String playerName,
        int assignedCountAfterSettlement,
        int totalRequired
    ) {
        int nextRound = round + 1;

        if (highestBid != null) {
            return new AuctionRoundSettlement(
                playerName,
                AuctionOutcome.SOLD,
                nextRound,
                assignedCountAfterSettlement >= totalRequired,
                highestBid
            );
        }

        return new AuctionRoundSettlement(
            playerName,
            AuctionOutcome.PASSED,
            nextRound,
            false,
            null
        );
    }

    private int highestAmount() {
        return highestBid == null ? 0 : highestBid.getAmount();
    }
}
