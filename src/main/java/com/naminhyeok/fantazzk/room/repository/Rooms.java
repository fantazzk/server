package com.naminhyeok.fantazzk.room.repository;

import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

public interface Rooms extends Repository<Room, RoomId> {
    Room save(Room room);

    Optional<Room> findById(RoomId id);

    Optional<Room> findByCode(String code);
}
