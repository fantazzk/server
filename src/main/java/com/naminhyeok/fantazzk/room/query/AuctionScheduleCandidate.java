package com.naminhyeok.fantazzk.room.query;

import java.time.Instant;

public record AuctionScheduleCandidate(
    String code,
    Instant deadline
) {
}
