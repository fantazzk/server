package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public record TeamLeaderView(
    String id,
    String nickname,
    Integer draftPosition,
    Integer remainingBudget
) {
    static TeamLeaderView from(RoomTeamLeader leader) {
        return new TeamLeaderView(
            leader.getId().value(),
            leader.getNickname(),
            leader.getDraftPosition(),
            leader.getRemainingBudget()
        );
    }
}
