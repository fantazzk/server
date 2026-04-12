package com.naminhyeok.fantazzk.room;

record DraftOrderSlotResponse(
    int draftPosition,
    String leaderId,
    String nickname
) {
    static DraftOrderSlotResponse empty(int draftPosition) {
        return new DraftOrderSlotResponse(draftPosition, null, null);
    }

    static DraftOrderSlotResponse from(int draftPosition, RoomTeamLeader leader) {
        return new DraftOrderSlotResponse(
            draftPosition,
            leader.getId().value(),
            leader.getNickname()
        );
    }
}
