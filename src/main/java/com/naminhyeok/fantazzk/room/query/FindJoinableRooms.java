package com.naminhyeok.fantazzk.room.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindJoinableRooms {
    private static final int JOINABLE_ROOM_LIMIT = 5;

    private final JoinableRoomReader joinableRoomReader;

    @Transactional(readOnly = true)
    public List<JoinableRoomResponse> list() {
        return joinableRoomReader.findLatestWaitingRooms(JOINABLE_ROOM_LIMIT).stream()
            .map(JoinableRoomResponse::from)
            .toList();
    }
}
