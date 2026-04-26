package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.TeamLeaderId;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JoinRoom {
    private final Rooms rooms;
    private final RoomRealtimeEventPublisher realtimeEventPublisher;

    @Transactional
    public RoomSessionResult join(String code, String nickname) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            TeamLeaderId joinedLeaderId = new TeamLeaderId(UUID.randomUUID().toString());
            RoomTeamLeader joinedLeader = room.join(joinedLeaderId, nickname, UUID.randomUUID().toString());
            Room saved = rooms.saveAndFlush(room);
            realtimeEventPublisher.publishRoomUpdatedAfterCommit(saved);
            return new RoomSessionResult(saved, joinedLeader);
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }
}
