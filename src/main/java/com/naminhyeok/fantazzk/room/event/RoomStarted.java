package com.naminhyeok.fantazzk.room.event;

import java.time.Instant;

public record RoomStarted(
    String roomCode,
    Instant roundEndsAt
) implements RoomSchedulingEvent {
}
