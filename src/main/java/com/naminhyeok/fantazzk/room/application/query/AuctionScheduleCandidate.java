package com.naminhyeok.fantazzk.room.application.query;

import java.time.Instant;

public record AuctionScheduleCandidate(
    String code,
    Instant deadline
) {
}
