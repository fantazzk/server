package com.naminhyeok.fantazzk.room.application.query;

public record JoinableRoomSummary(
    String code,
    String mode,
    int teamCount,
    int joinedLeaderCount,
    int remainingSlotCount,
    String startReadiness
) {
}
