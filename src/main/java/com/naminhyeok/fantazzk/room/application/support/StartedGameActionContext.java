package com.naminhyeok.fantazzk.room.application.support;

import com.naminhyeok.fantazzk.room.domain.game.Game;
import com.naminhyeok.fantazzk.room.domain.room.Room;
import com.naminhyeok.fantazzk.room.domain.room.RoomTeamLeader;
import java.util.Objects;

public record StartedGameActionContext(
    Room room,
    Game game,
    RoomTeamLeader caller
) {
    public StartedGameActionContext {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(game, "game must not be null");
        Objects.requireNonNull(caller, "caller must not be null");
    }
}
