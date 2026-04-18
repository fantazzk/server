package com.naminhyeok.fantazzk.room.application.query;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.room.JoinableRoomView;
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
    public List<JoinableRoomView> list() {
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
