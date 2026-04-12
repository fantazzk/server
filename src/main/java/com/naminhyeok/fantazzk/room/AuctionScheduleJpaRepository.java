package com.naminhyeok.fantazzk.room;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

interface AuctionScheduleJpaRepository extends Repository<Room, RoomId> {
    List<Room> findByStatusAndModeOrderByCodeAsc(RoomStatus status, RoomMode mode, Pageable pageable);
}
