package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public record GameParticipantView(
    String teamLeaderId,
    String nickname,
    Integer draftPosition,
    Integer remainingBudget
) {
    static GameParticipantView from(GameParticipant participant) {
        return new GameParticipantView(
            participant.teamLeaderId().value(),
            participant.nickname(),
            participant.draftPosition(),
            participant.remainingBudget()
        );
    }
}
