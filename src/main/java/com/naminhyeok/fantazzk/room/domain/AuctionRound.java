package com.naminhyeok.fantazzk.room.domain;

import org.springframework.lang.Nullable;

public final class AuctionRound {
    private final int round;
    private final RoomBid highestBid;

    public AuctionRound(int round, RoomBid highestBid) {
        require(round > 0, "경매 라운드는 1 이상이어야 합니다");
        this.round = round;
        this.highestBid = highestBid;
    }

    public int getRound() {
        return round;
    }

    @Nullable
    public RoomBid getHighestBid() {
        return highestBid;
    }

    public void requireHigherBid(int amount) {
        require(amount > highestAmount(), "현재 최고가보다 높아야 합니다");
    }

    public void requireRosterCapacity(int currentMemberCount, int picksPerTeam) {
        checkState(currentMemberCount < picksPerTeam, "팀장의 팀원 정원이 가득 찼습니다");
    }

    public AuctionRoundSettlement settle(String playerName, int assignedCountAfterSettlement, int totalRequired) {
        int nextRound = round + 1;
        if (highestBid != null) {
            return new AuctionRoundSettlement(
                    playerName,
                    AuctionOutcome.SOLD,
                    nextRound,
                    assignedCountAfterSettlement >= totalRequired,
                    highestBid);
        }
        return new AuctionRoundSettlement(
                playerName,
                AuctionOutcome.PASSED,
                nextRound,
                false,
                null);
    }

    private int highestAmount() {
        return highestBid == null ? 0 : highestBid.getAmount();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
