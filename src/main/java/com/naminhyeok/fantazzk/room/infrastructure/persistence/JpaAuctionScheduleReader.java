package com.naminhyeok.fantazzk.room.infrastructure.persistence;

import com.naminhyeok.fantazzk.room.application.query.AuctionScheduleCandidate;
import com.naminhyeok.fantazzk.room.application.query.AuctionScheduleReader;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaAuctionScheduleReader implements AuctionScheduleReader {
    private static final int CATCH_UP_BATCH_SIZE = 200;

    private final AuctionScheduleJpaRepository repository;

    @Override
    public List<AuctionScheduleCandidate> findInProgressAuctionSchedules() {
        List<AuctionScheduleCandidate> candidates = new ArrayList<>();
        int offset = 0;
        while (true) {
            List<AuctionScheduleCandidate> batch = repository.findInProgressAuctionSchedules(CATCH_UP_BATCH_SIZE, offset);
            if (batch.isEmpty()) {
                return candidates;
            }
            candidates.addAll(batch);
            if (batch.size() < CATCH_UP_BATCH_SIZE) {
                return candidates;
            }
            offset += CATCH_UP_BATCH_SIZE;
        }
    }
}
