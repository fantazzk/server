package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.room.application.room.RoomSessionResult;
public record RoomSessionView(
    RoomView room,
    TeamLeaderSessionView teamLeaderSession
) {
    static RoomSessionView from(RoomSessionResult result) {
        return from(result.room(), result.leader());
    }

    static RoomSessionView fromHost(Room room) {
        RoomTeamLeader host = room.getLeaders().stream()
            .filter(leader -> leader.getId().equals(room.getHostLeaderId()))
            .findFirst()
            .orElseThrow();
        return from(room, host);
    }

    static RoomSessionView from(Room room, RoomTeamLeader leader) {
        return new RoomSessionView(
            RoomView.from(room),
            TeamLeaderSessionView.from(room, leader)
        );
    }
}
