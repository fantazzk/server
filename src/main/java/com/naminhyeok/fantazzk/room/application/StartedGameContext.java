package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.Room;
import java.util.Objects;

public record StartedGameContext(
    Room room,
    Game game
) {
    public StartedGameContext {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(game, "game must not be null");
    }
}
