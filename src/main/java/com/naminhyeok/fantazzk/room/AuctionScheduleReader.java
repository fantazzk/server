package com.naminhyeok.fantazzk.room;

import java.util.List;

interface AuctionScheduleReader {
    List<AuctionScheduleCandidate> findInProgressAuctionSchedules();
}
