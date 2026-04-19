package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.query.AuctionScheduleCandidate;
import java.util.List;

public interface AuctionScheduleReader {
    public List<AuctionScheduleCandidate> findInProgressAuctionSchedules();
}
