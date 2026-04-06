package com.naminhyeok.fantazzk.room.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public final class RoomPlayerId implements Identifier, Serializable {
    private final UUID value;

    private RoomPlayerId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static RoomPlayerId random() {
        return new RoomPlayerId(UUID.randomUUID());
    }

    public static RoomPlayerId from(UUID value) {
        return new RoomPlayerId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomPlayerId roomPlayerId)) {
            return false;
        }
        return value.equals(roomPlayerId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
