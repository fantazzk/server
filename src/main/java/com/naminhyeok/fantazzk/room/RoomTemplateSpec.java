package com.naminhyeok.fantazzk.room;

import java.util.List;

record RoomTemplateSpec(
    Mode mode,
    int teamCount,
    int teamSize,
    Integer budget,
    int pickBanTime,
    Integer minBidUnit,
    DraftOrderStrategy draftOrderStrategy,
    List<Player> players
) {
    RoomTemplateSpec(
        Mode mode,
        int teamCount,
        int teamSize,
        Integer budget,
        int pickBanTime,
        DraftOrderStrategy draftOrderStrategy,
        List<Player> players
    ) {
        this(mode, teamCount, teamSize, budget, pickBanTime, null, draftOrderStrategy, players);
    }

    public enum Mode {
        AUCTION,
        DRAFT
    }

    public enum DraftOrderStrategy {
        SNAKE,
        FIXED
    }

    public record Player(RoomPlayerId id, String name, String position, int displayOrder) {
    }
}
