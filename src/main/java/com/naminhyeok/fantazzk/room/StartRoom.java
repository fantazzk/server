package com.naminhyeok.fantazzk.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StartRoom {
    private final Rooms rooms;

    @Transactional
    public void start(String code) {
        Room room = rooms.findByCode(code).orElseThrow(() -> RoomException.notFound(code));
        room.start();
        rooms.save(room);
    }
}
