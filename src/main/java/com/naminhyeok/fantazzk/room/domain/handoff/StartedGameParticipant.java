package com.naminhyeok.fantazzk.room.domain.handoff;

import com.naminhyeok.fantazzk.room.domain.room.RoomMode;
import com.naminhyeok.fantazzk.room.domain.shared.TeamLeaderId;

public sealed interface StartedGameParticipant permits StartedAuctionParticipant, StartedDraftParticipant {
    TeamLeaderId teamLeaderId();

    String nickname();

    RoomMode mode();

    default Integer draftPosition() {
        return null;
    }

    default Integer remainingBudget() {
        return null;
    }
}
