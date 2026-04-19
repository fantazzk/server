package com.naminhyeok.fantazzk.room.domain;

import java.util.Objects;

public record StartedRoomSnapshot(
    Room room,
    Game game
) {
    public StartedRoomSnapshot {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(game, "game must not be null");
    }
}
