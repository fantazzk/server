package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public record GameMemberView(
    String teamLeaderId,
    String playerName,
    int assignOrder
) {
    static GameMemberView from(RosterMember member) {
        return new GameMemberView(
            member.teamLeaderId().value(),
            member.playerName(),
            member.assignOrder()
        );
    }
}
