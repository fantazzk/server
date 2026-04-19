package com.naminhyeok.fantazzk.room.infrastructure.persistence;

import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import com.naminhyeok.fantazzk.room.domain.RoomStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

public interface JoinableRoomJpaRepository extends Repository<Room, RoomId> {
    @EntityGraph(attributePaths = "leaders")
    public List<Room> findByStatusOrderByCreatedAtDescCodeDesc(RoomStatus status, Pageable pageable);
}
