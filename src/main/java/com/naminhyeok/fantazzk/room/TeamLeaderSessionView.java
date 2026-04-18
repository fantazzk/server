package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public record TeamLeaderSessionView(
    String leaderId,
    String role,
    String actionToken
) {
    static TeamLeaderSessionView from(Room room, RoomTeamLeader leader) {
        return new TeamLeaderSessionView(
            leader.getId().value(),
            room.getHostLeaderId().equals(leader.getId()) ? "HOST" : "LEADER",
            leader.getActionToken()
        );
    }
}
