package com.naminhyeok.fantazzk.room;

import java.io.Serializable;
import java.util.Objects;

public final class RoomCode implements Serializable {
    private final String value;

    private RoomCode(String value) {
        this.value = validate(value);
    }

    public static RoomCode of(String value) {
        return new RoomCode(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RoomCode roomCode)) {
            return false;
        }
        return value.equals(roomCode.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }

    private static String validate(String value) {
        String normalized = Objects.requireNonNull(value, "value").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("방 코드는 비어 있을 수 없습니다");
        }
        return normalized;
    }
}
