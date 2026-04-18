package com.naminhyeok.fantazzk.room.infrastructure.persistence;

import com.naminhyeok.fantazzk.room.JoinableRoomView;
import com.naminhyeok.fantazzk.room.application.query.JoinableRoomReader;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class JpaJoinableRoomReader implements JoinableRoomReader {
    private static final int WAITING_ROOM_PAGE_SIZE = 5;

    private final JoinableRoomJpaRepository repository;

    @Override
    public List<JoinableRoomView> findLatestWaitingRooms(int limit) {
        List<JoinableRoomView> joinableRooms = new ArrayList<>();
        int offset = 0;
        while (joinableRooms.size() < limit) {
            List<JoinableRoomView> waitingRooms = repository.findLatestWaitingRooms(WAITING_ROOM_PAGE_SIZE, offset);
            if (waitingRooms.isEmpty()) {
                return joinableRooms;
            }
            joinableRooms.addAll(waitingRooms.stream().limit(limit - joinableRooms.size()).toList());
            if (waitingRooms.size() < WAITING_ROOM_PAGE_SIZE) {
                return joinableRooms;
            }
            offset += WAITING_ROOM_PAGE_SIZE;
        }
        return joinableRooms;
    }
}
