package com.naminhyeok.fantazzk.room;

import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

@Getter
class RoomPlayer implements Entity<Room, RoomPlayer.RoomPlayerId> {
    private final RoomPlayerId id;
    private final String name;
    private final int displayOrder;

    RoomPlayer(String name, int displayOrder) {
        this.id = new RoomPlayerId(UUID.randomUUID());
        this.name = name;
        this.displayOrder = displayOrder;
    }

    @Override
    public RoomPlayerId getId() {
        return id;
    }

    record RoomPlayerId(UUID roomPlayerId) implements Identifier {
    }
}
