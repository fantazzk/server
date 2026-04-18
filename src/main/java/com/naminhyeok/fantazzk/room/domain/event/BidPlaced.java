package com.naminhyeok.fantazzk.room.domain.event;

import java.time.Instant;

public record BidPlaced(
    String roomCode,
    String leaderId,
    int amount,
    int round,
    Instant roundEndsAt
) implements RoomSchedulingEvent {
}
