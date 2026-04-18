package com.naminhyeok.fantazzk.room;

import java.util.Objects;

record GameRules(
    Mode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    int pickBanTime,
    Integer minBidUnit,
    Integer positionLimit,
    RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy
) {
    static GameRules auction(int teamCount, int teamSize, int budget, int pickBanTime, int minBidUnit, Integer positionLimit) {
        return new GameRules(Mode.AUCTION, teamCount, teamSize, budget, pickBanTime, minBidUnit, positionLimit, null);
    }

    static GameRules draft(int teamCount, int teamSize, int pickBanTime, RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy) {
        return new GameRules(
            Mode.DRAFT,
            teamCount,
            teamSize,
            null,
            pickBanTime,
            null,
            null,
            Objects.requireNonNull(draftOrderStrategy)
        );
    }

    AuctionRules auctionRules() {
        if (mode != Mode.AUCTION || budget == null || minBidUnit == null) {
            throw new IllegalStateException("auction rules are not available");
        }
        return new AuctionRules(budget, pickBanTime, minBidUnit, positionLimit);
    }

    DraftRules draftRules() {
        if (mode != Mode.DRAFT || draftOrderStrategy == null) {
            throw new IllegalStateException("draft rules are not available");
        }
        return new DraftRules(pickBanTime, draftOrderStrategy);
    }

    enum Mode {
        AUCTION,
        DRAFT
    }

    record AuctionRules(
        int budget,
        int pickBanTime,
        int minBidUnit,
        Integer positionLimit
    ) {
    }

    record DraftRules(
        int pickBanTime,
        RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy
    ) {
    }
}
