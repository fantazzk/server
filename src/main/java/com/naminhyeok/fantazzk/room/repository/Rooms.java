package com.naminhyeok.fantazzk.room.repository;

import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import java.util.Optional;
import org.jmolecules.ddd.types.Repository;

public interface Rooms extends Repository<Room, RoomId> {
    public Room save(Room room);

    public Room saveAndFlush(Room room);

    public Optional<Room> findById(RoomId id);

    public Optional<Room> findByCode(String code);
}
