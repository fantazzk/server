package com.naminhyeok.fantazzk.room;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.jmolecules.ddd.types.Repository;

interface Rooms extends Repository<Room, RoomId> {
    Room save(Room room);

    Optional<Room> findById(RoomId id);

    Optional<Room> findByCode(String code);

    Slice<Room> findByStatusOrderByCreatedAtDesc(RoomStatus status, Pageable pageable);
}
