package com.naminhyeok.fantazzk.room.domain;

import org.jmolecules.ddd.types.Identifier;

public record RoomBidId(long value) implements Identifier {
    public RoomBidId {
        if (value <= 0L) {
            throw new IllegalArgumentException("RoomBidId는 0보다 커야 합니다");
        }
    }
}
