package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;

public record RoomSessionResult(
    Room room,
    RoomTeamLeader leader
) {
}
