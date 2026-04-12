package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record AuctionScheduleCandidate(
    String code,
    Instant deadline
) {
}
