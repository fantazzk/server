package com.naminhyeok.fantazzk.room.domain;

import org.jmolecules.ddd.types.Identifier;

public record RoomTeamLeaderId(long value) implements Identifier {
    public RoomTeamLeaderId {
        if (value <= 0L) {
            throw new IllegalArgumentException("RoomTeamLeaderId는 0보다 커야 합니다");
        }
    }
}
