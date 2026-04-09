package com.naminhyeok.fantazzk.room;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class FindJoinableRooms {
    private static final int JOINABLE_ROOM_LIMIT = 5;

    private final Rooms rooms;

    @Transactional(readOnly = true)
    public List<Room> list() {
        return rooms.findJoinableWaitingRooms(PageRequest.of(0, JOINABLE_ROOM_LIMIT)).stream()
            .filter(Room::isJoinable)
            .limit(JOINABLE_ROOM_LIMIT)
            .toList();
    }
}
