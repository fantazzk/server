package com.naminhyeok.fantazzk.room;

import java.util.Objects;

record StartedRoomSnapshot(
    Room room,
    Game game
) {
    StartedRoomSnapshot {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(game, "game must not be null");
    }
}
