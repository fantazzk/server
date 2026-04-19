package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateRoomAttempt {
    private final Rooms rooms;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Room save(Room room) {
        return rooms.saveAndFlush(room);
    }
}
