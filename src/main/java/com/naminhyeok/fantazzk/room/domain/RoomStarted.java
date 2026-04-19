package com.naminhyeok.fantazzk.room.domain;

import java.time.Instant;

public record RoomStarted(
    String roomCode,
    Instant roundEndsAt
) implements RoomSchedulingEvent {
}
