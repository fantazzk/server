package com.naminhyeok.fantazzk.room.application.support;

import com.naminhyeok.fantazzk.room.GameView;
import java.util.Objects;

public record StartedRoomSnapshot(
    String roomCode,
    long snapshotVersion,
    GameView game
) {
    public StartedRoomSnapshot {
        Objects.requireNonNull(game, "game must not be null");
        Objects.requireNonNull(roomCode, "roomCode must not be null");
    }
}
