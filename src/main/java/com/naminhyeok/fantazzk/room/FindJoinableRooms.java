package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.application.query.JoinableRoomReader;
import com.naminhyeok.fantazzk.room.application.query.JoinableRoomSummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class FindJoinableRooms {
    private static final int JOINABLE_ROOM_LIMIT = 5;

    private final JoinableRoomReader joinableRoomReader;

    @Transactional(readOnly = true)
    List<JoinableRoomView> list() {
        return joinableRoomReader.findLatestWaitingRooms(JOINABLE_ROOM_LIMIT).stream()
            .map(FindJoinableRooms::toView)
            .toList();
    }

    private static JoinableRoomView toView(JoinableRoomSummary summary) {
        return new JoinableRoomView(
            summary.code(),
            summary.mode(),
            summary.teamCount(),
            summary.joinedLeaderCount(),
            summary.remainingSlotCount(),
            summary.startReadiness()
        );
    }
}
