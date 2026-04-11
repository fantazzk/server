package com.naminhyeok.fantazzk.room;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class FindJoinableRooms {
    private static final int JOINABLE_ROOM_LIMIT = 5;
    private static final int WAITING_ROOM_PAGE_SIZE = 5;

    private final Rooms rooms;

    @Transactional(readOnly = true)
    public List<Room> list() {
        List<Room> joinableRooms = new ArrayList<>();
        int page = 0;
        while (joinableRooms.size() < JOINABLE_ROOM_LIMIT) {
            List<Room> waitingRooms =
                rooms.findByStatusOrderByCreatedAtDescCodeDesc(
                    RoomStatus.WAITING,
                    PageRequest.of(page, WAITING_ROOM_PAGE_SIZE)
                );
            if (waitingRooms.isEmpty()) {
                return joinableRooms;
            }
            joinableRooms.addAll(
                waitingRooms.stream()
                    .filter(Room::isJoinable)
                    .limit(JOINABLE_ROOM_LIMIT - joinableRooms.size())
                    .toList()
            );
            if (waitingRooms.size() < WAITING_ROOM_PAGE_SIZE) {
                return joinableRooms;
            }
            page += 1;
        }
        return joinableRooms;
    }
}
