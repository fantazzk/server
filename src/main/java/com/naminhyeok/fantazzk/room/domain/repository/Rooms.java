package com.naminhyeok.fantazzk.room.domain.repository;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.RoomId;

import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

public interface Rooms extends Repository<Room, RoomId> {
    Room save(Room room);

    Room saveAndFlush(Room room);

    Optional<Room> findById(RoomId id);

    Optional<Room> findByCode(String code);
}
