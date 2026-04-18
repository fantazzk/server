package com.naminhyeok.fantazzk.room;

public record DraftOrderSlotView(
    int draftPosition,
    String leaderId,
    String nickname
) {
    static DraftOrderSlotView empty(int draftPosition) {
        return new DraftOrderSlotView(draftPosition, null, null);
    }

    static DraftOrderSlotView from(int draftPosition, RoomTeamLeader leader) {
        return new DraftOrderSlotView(
            draftPosition,
            leader.getId().value(),
            leader.getNickname()
        );
    }
}
