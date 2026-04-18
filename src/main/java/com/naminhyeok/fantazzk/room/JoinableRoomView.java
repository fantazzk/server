package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public record JoinableRoomView(
    String code,
    String mode,
    int teamCount,
    int joinedLeaderCount,
    int remainingSlotCount,
    String startReadiness
) {
    static JoinableRoomView from(Room room) {
        int joinedLeaderCount = room.getLeaders().size();
        return new JoinableRoomView(
            room.getCode(),
            room.getMode().name(),
            room.getTeamCount(),
            joinedLeaderCount,
            room.getTeamCount() - joinedLeaderCount,
            room.getStartReadiness().name()
        );
    }
}
