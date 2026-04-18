package com.naminhyeok.fantazzk.room.application.support;

import com.naminhyeok.fantazzk.room.RoomView;
import java.util.Objects;

public record RoomSnapshot(
    String roomCode,
    long snapshotVersion,
    RoomView room
) {
    public RoomSnapshot {
        Objects.requireNonNull(roomCode, "roomCode must not be null");
        Objects.requireNonNull(room, "room must not be null");
    }
}
