package com.naminhyeok.fantazzk.room;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class CreateRoomAttempt {
    private final Rooms rooms;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Room save(Room room, Consumer<Room> followUp) {
        Room saved = rooms.saveAndFlush(room);
        followUp.accept(saved);
        return saved;
    }
}
