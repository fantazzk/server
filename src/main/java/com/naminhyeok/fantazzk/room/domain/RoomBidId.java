package com.naminhyeok.fantazzk.room.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public final class RoomBidId implements Identifier, Serializable {
    private final UUID value;

    private RoomBidId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static RoomBidId random() {
        return new RoomBidId(UUID.randomUUID());
    }

    public static RoomBidId from(UUID value) {
        return new RoomBidId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomBidId roomBidId)) {
            return false;
        }
        return value.equals(roomBidId.value);
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
