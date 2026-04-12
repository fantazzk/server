package com.naminhyeok.fantazzk.room;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaJoinableRoomReader implements JoinableRoomReader {
    private static final int WAITING_ROOM_PAGE_SIZE = 5;

    private final JoinableRoomJpaRepository repository;

    @Override
    public List<Room> findLatestWaitingRooms(int limit) {
        List<Room> joinableRooms = new ArrayList<>();
        int page = 0;
        while (joinableRooms.size() < limit) {
            List<Room> waitingRooms =
                repository.findByStatusOrderByCreatedAtDescCodeDesc(
                    RoomStatus.WAITING,
                    PageRequest.of(page, WAITING_ROOM_PAGE_SIZE)
                );
            if (waitingRooms.isEmpty()) {
                return joinableRooms;
            }
            joinableRooms.addAll(
                waitingRooms.stream()
                    .filter(Room::isJoinable)
                    .limit(limit - joinableRooms.size())
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
