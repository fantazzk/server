package com.naminhyeok.fantazzk.room.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import org.jmolecules.ddd.types.Identifier;

public final class RoomTeamLeaderId implements Identifier, Serializable {
    private final UUID value;

    private RoomTeamLeaderId(UUID value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public static RoomTeamLeaderId random() {
        return new RoomTeamLeaderId(UUID.randomUUID());
    }

    public static RoomTeamLeaderId from(UUID value) {
        return new RoomTeamLeaderId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomTeamLeaderId roomTeamLeaderId)) {
            return false;
        }
        return value.equals(roomTeamLeaderId.value);
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
