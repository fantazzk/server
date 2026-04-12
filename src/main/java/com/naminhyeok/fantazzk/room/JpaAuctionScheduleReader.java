package com.naminhyeok.fantazzk.room;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaAuctionScheduleReader implements AuctionScheduleReader {
    private static final int CATCH_UP_BATCH_SIZE = 200;

    private final AuctionScheduleJpaRepository repository;

    @Override
    public List<AuctionScheduleCandidate> findInProgressAuctionSchedules() {
        List<AuctionScheduleCandidate> candidates = new ArrayList<>();
        int page = 0;
        while (true) {
            List<Room> batch =
                repository.findByStatusAndModeOrderByCodeAsc(
                    RoomStatus.IN_PROGRESS,
                    RoomMode.AUCTION,
                    PageRequest.of(page, CATCH_UP_BATCH_SIZE)
                );
            if (batch.isEmpty()) {
                return candidates;
            }
            candidates.addAll(batch.stream().map(room -> new AuctionScheduleCandidate(room.getCode(), room.getCurrentAuctionRoundEndsAt())).toList());
            if (batch.size() < CATCH_UP_BATCH_SIZE) {
                return candidates;
            }
            page += 1;
        }
    }
}
