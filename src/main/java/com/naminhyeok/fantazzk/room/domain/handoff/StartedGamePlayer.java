package com.naminhyeok.fantazzk.room.domain.handoff;

import com.naminhyeok.fantazzk.room.domain.room.RoomPlayerId;
import java.util.Objects;

public record StartedGamePlayer(
    RoomPlayerId playerId,
    String name,
    String position,
    int displayOrder
) {
    public StartedGamePlayer {
        Objects.requireNonNull(playerId, "playerId must not be null");
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(position, "position must not be null");
    }
}
