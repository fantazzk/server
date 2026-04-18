package com.naminhyeok.fantazzk.room.application.query;

import java.util.List;

public interface AuctionScheduleReader {
    List<AuctionScheduleCandidate> findInProgressAuctionSchedules();
}
