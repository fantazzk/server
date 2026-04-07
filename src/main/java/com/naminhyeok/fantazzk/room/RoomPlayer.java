package com.naminhyeok.fantazzk.room;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

@Getter
class RoomPlayer implements Entity<Room, RoomPlayer.RoomPlayerId> {
    private final RoomPlayerId id;
    private final String name;
    private int displayOrder;
    @Enumerated(EnumType.STRING)
    private PlayerStatus status;

    RoomPlayer(String name, int displayOrder) {
        this.id = new RoomPlayerId(UUID.randomUUID());
        this.name = name;
        this.displayOrder = displayOrder;
        this.status = PlayerStatus.AVAILABLE;
    }

    @Override
    public RoomPlayerId getId() {
        return id;
    }

    void assign() {
        this.status = PlayerStatus.ASSIGNED;
    }

    void moveToBack(int nextOrder) {
        this.displayOrder = nextOrder;
    }

    record RoomPlayerId(UUID roomPlayerId) implements Identifier {
    }
}
