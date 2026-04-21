package com.naminhyeok.fantazzk.room.infrastructure.persistence;

import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.query.AuctionScheduleCandidate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaAuctionScheduleReader {
    private static final int CATCH_UP_BATCH_SIZE = 200;

    private final AuctionScheduleJpaRepository repository;

    public List<AuctionScheduleCandidate> findInProgressAuctionSchedules() {
        List<AuctionScheduleCandidate> candidates = new ArrayList<>();
        int page = 0;
        while (true) {
            List<AuctionGame> batch = repository.findByCurrentRoundEndsAtNotNullOrderByRoomCodeAsc(PageRequest.of(page, CATCH_UP_BATCH_SIZE));
            if (batch.isEmpty()) {
                return candidates;
            }
            candidates.addAll(batch.stream().map(game -> new AuctionScheduleCandidate(game.getRoomCode(), game.getCurrentRoundEndsAt())).toList());
            if (batch.size() < CATCH_UP_BATCH_SIZE) {
                return candidates;
            }
            page += 1;
        }
    }
}
