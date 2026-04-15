package com.naminhyeok.fantazzk.room;

record GameRules(
    int teamCount,
    int teamSize,
    Integer budget,
    int pickBanTime,
    Integer minBidUnit,
    Integer positionLimit,
    RoomTemplateSpec.DraftOrderStrategy draftOrderStrategy
) {
}
