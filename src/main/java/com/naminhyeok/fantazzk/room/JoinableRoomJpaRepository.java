package com.naminhyeok.fantazzk.room;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

interface JoinableRoomJpaRepository extends Repository<Room, RoomId> {
    @EntityGraph(attributePaths = "leaders")
    List<Room> findByStatusOrderByCreatedAtDescCodeDesc(RoomStatus status, Pageable pageable);
}
