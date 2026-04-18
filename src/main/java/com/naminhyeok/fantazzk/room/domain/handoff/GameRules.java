package com.naminhyeok.fantazzk.room.domain.handoff;

import com.naminhyeok.fantazzk.room.domain.room.RoomMode;
import com.naminhyeok.fantazzk.room.domain.shared.DraftOrderStrategy;
import java.util.Objects;

public record GameRules(
    RoomMode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    int pickBanTime,
    Integer minBidUnit,
    Integer positionLimit,
    DraftOrderStrategy draftOrderStrategy
) {
    public static GameRules auction(
        int teamCount,
        int teamSize,
        int budget,
        int pickBanTime,
        int minBidUnit,
        Integer positionLimit
    ) {
        return new GameRules(RoomMode.AUCTION, teamCount, teamSize, budget, pickBanTime, minBidUnit, positionLimit, null);
    }

    public static GameRules draft(int teamCount, int teamSize, int pickBanTime, DraftOrderStrategy draftOrderStrategy) {
        return new GameRules(
            RoomMode.DRAFT,
            teamCount,
            teamSize,
            null,
            pickBanTime,
            null,
            null,
            Objects.requireNonNull(draftOrderStrategy)
        );
    }

    public AuctionRules auctionRules() {
        if (mode != RoomMode.AUCTION || budget == null || minBidUnit == null) {
            throw new IllegalStateException("auction rules are not available");
        }
        return new AuctionRules(budget, pickBanTime, minBidUnit, positionLimit);
    }

    public DraftRules draftRules() {
        if (mode != RoomMode.DRAFT || draftOrderStrategy == null) {
            throw new IllegalStateException("draft rules are not available");
        }
        return new DraftRules(pickBanTime, draftOrderStrategy);
    }

    public record AuctionRules(
        int budget,
        int pickBanTime,
        int minBidUnit,
        Integer positionLimit
    ) {
    }

    public record DraftRules(
        int pickBanTime,
        DraftOrderStrategy draftOrderStrategy
    ) {
    }
}
