package com.naminhyeok.fantazzk.room.query;

import java.util.List;

public interface AuctionScheduleReader {
    List<AuctionScheduleCandidate> findInProgressAuctionSchedules();
}
