package com.naminhyeok.fantazzk.room.application.support;

import java.time.Instant;

public record AuctionSettled(
    String roomCode,
    String outcome,
    Instant roundEndsAt
) implements RoomSchedulingEvent {
}
