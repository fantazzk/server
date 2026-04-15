package com.naminhyeok.fantazzk.room;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

interface AuctionScheduleJpaRepository extends Repository<AuctionGame, GameId> {
    List<AuctionGame> findByCurrentRoundEndsAtNotNullOrderByRoomCodeAsc(Pageable pageable);
}
