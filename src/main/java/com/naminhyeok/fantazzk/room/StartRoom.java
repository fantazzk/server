package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class StartRoom {
    private final Rooms rooms;

    @Transactional
    public void start(String code) {
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        room.start();
        rooms.save(room);
    }
}
