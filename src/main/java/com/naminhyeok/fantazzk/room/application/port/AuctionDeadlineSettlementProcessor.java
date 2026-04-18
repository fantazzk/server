package com.naminhyeok.fantazzk.room.application.port;

import java.time.Instant;

public interface AuctionDeadlineSettlementProcessor {
    Instant processDueAuction(String code);
}
