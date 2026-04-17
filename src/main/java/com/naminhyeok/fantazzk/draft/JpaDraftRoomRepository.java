package com.naminhyeok.fantazzk.draft;

import java.util.Optional;
import org.springframework.data.repository.Repository;

interface JpaDraftRoomRepository extends Repository<JpaDraftRoomEntity, String> {
    JpaDraftRoomEntity save(JpaDraftRoomEntity entity);

    Optional<JpaDraftRoomEntity> findById(String roomCode);
}
