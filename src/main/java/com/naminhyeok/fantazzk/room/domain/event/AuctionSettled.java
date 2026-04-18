package com.naminhyeok.fantazzk.room.domain.event;

import java.time.Instant;

public record AuctionSettled(
    String roomCode,
    String outcome,
    Instant roundEndsAt
) implements RoomSchedulingEvent {
}
