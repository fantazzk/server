package com.naminhyeok.fantazzk.room;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public final class RoomId implements Identifier, Serializable {
    private final UUID value;

    private RoomId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static RoomId random() {
        return new RoomId(UUID.randomUUID());
    }

    public static RoomId from(UUID value) {
        return new RoomId(value);
    }

    public static RoomId from(String value) {
        return from(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomId roomId)) {
            return false;
        }
        return value.equals(roomId.value);
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
