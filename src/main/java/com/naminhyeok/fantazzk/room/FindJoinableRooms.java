package com.naminhyeok.fantazzk.room;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class FindJoinableRooms {
    private static final int JOINABLE_ROOM_LIMIT = 5;

    private final Rooms rooms;

    @Transactional(readOnly = true)
    public List<Room> list() {
        List<Room> joinableRooms = new ArrayList<>(JOINABLE_ROOM_LIMIT);

        for (int page = 0; joinableRooms.size() < JOINABLE_ROOM_LIMIT; page++) {
            Slice<Room> waitingRooms =
                rooms.findByStatusOrderByCreatedAtDesc(RoomStatus.WAITING, PageRequest.of(page, JOINABLE_ROOM_LIMIT));

            if (waitingRooms.isEmpty()) {
                break;
            }

            waitingRooms.stream()
                .filter(Room::isJoinable)
                .forEach(joinableRooms::add);

            if (!waitingRooms.hasNext()) {
                break;
            }
        }

        return joinableRooms.stream().limit(JOINABLE_ROOM_LIMIT).toList();
    }
}
