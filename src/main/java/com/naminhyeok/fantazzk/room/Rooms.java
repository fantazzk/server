package com.naminhyeok.fantazzk.room;

import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

public interface Rooms extends Repository<Room, RoomId> {
    Room save(Room room);

    Optional<Room> findById(RoomId id);

    Optional<Room> findByCode(String code);
}
