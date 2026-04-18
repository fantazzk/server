package com.naminhyeok.fantazzk.room.application.support;

import java.time.Instant;

public record RoomStarted(
    String roomCode,
    Instant roundEndsAt
) implements RoomSchedulingEvent {
}
