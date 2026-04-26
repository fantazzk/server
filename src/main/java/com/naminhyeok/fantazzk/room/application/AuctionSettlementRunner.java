package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.room.domain.Room;

public interface AuctionSettlementRunner {
    Room settleIfDue(String code);
}
