package com.naminhyeok.fantazzk.room.domain;

import org.jmolecules.ddd.types.Identifier;

public record RoomPlayerId(long value) implements Identifier {
    public RoomPlayerId {
        if (value <= 0L) {
            throw new IllegalArgumentException("RoomPlayerId는 0보다 커야 합니다");
        }
    }
}
