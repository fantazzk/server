package com.naminhyeok.fantazzk.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CreateRoomAttempt {
    private final Rooms rooms;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Room save(Room room) {
        return rooms.saveAndFlush(room);
    }
}
