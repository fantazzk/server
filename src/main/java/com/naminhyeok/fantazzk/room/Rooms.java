package com.naminhyeok.fantazzk.room;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.jmolecules.ddd.types.Repository;

interface Rooms extends Repository<Room, RoomId> {
    Room save(Room room);

    Room saveAndFlush(Room room);

    Optional<Room> findById(RoomId id);

    Optional<Room> findByCode(String code);

    @Query("""
        select r
        from Room r
        where r.status = com.naminhyeok.fantazzk.room.RoomStatus.WAITING
          and size(r.leaders) < r.teamCount
        order by r.createdAt desc, r.code desc
        """)
    List<Room> findJoinableWaitingRooms(Pageable pageable);

    @Query("""
        select r
        from Room r
        where r.status = com.naminhyeok.fantazzk.room.RoomStatus.IN_PROGRESS
          and r.mode = com.naminhyeok.fantazzk.room.RoomMode.AUCTION
        order by
          case when r.currentAuctionRoundEndsAt is null then 0 else 1 end,
          r.currentAuctionRoundEndsAt asc,
          r.code asc
        """)
    List<Room> findSchedulableAuctionRooms(Pageable pageable);
}
