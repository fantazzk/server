package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record AuctionSettled(
    String roomCode,
    String outcome,
    Instant roundEndsAt
) implements RoomSchedulingEvent {
}
