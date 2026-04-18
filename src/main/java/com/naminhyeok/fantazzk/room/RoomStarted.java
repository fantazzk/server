package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record RoomStarted(
    String roomCode,
    Instant roundEndsAt
) implements RoomSchedulingEvent {
}
