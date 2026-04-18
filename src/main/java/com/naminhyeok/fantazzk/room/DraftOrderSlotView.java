package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

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
