package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record BidPlaced(
    String roomCode,
    String leaderId,
    int amount,
    int round,
    Instant roundEndsAt
) implements RoomSchedulingEvent {
}
