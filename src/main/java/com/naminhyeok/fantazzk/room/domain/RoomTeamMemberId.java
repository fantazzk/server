package com.naminhyeok.fantazzk.room.domain;

import org.jmolecules.ddd.types.Identifier;

public record RoomTeamMemberId(long value) implements Identifier {
    public RoomTeamMemberId {
        if (value <= 0L) {
            throw new IllegalArgumentException("RoomTeamMemberId는 0보다 커야 합니다");
        }
    }
}
