package com.naminhyeok.fantazzk.room;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.jmolecules.ddd.types.Repository;

interface Rooms extends Repository<Room, RoomId> {
    Room save(Room room);

    Room saveAndFlush(Room room);

    Optional<Room> findById(RoomId id);

    Optional<Room> findByCode(String code);

    List<Room> findByStatusOrderByCreatedAtDescCodeDesc(RoomStatus status, Pageable pageable);

    List<Room> findByStatusAndModeOrderByCodeAsc(RoomStatus status, RoomMode mode, Pageable pageable);
}
