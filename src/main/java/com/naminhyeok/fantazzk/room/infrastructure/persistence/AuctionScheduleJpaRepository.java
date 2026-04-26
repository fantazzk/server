package com.naminhyeok.fantazzk.room.infrastructure.persistence;

import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.GameId;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface AuctionScheduleJpaRepository extends Repository<AuctionGame, GameId> {
    List<AuctionGame> findByCurrentRoundEndsAtNotNullOrderByRoomCodeAsc(Pageable pageable);
}
