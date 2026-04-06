package com.naminhyeok.fantazzk.room.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public final class RoomTeamMemberId implements Identifier, Serializable {
    private final UUID value;

    private RoomTeamMemberId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static RoomTeamMemberId random() {
        return new RoomTeamMemberId(UUID.randomUUID());
    }

    public static RoomTeamMemberId from(UUID value) {
        return new RoomTeamMemberId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomTeamMemberId roomTeamMemberId)) {
            return false;
        }
        return value.equals(roomTeamMemberId.value);
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
