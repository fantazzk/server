package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClearDraftPosition {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final RoomRealtimeEventPublisher realtimeEventPublisher;

    @Transactional
    public Room clear(String code, String actionToken) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            room.clearDraftPosition(caller.getId());
            Room saved = rooms.saveAndFlush(room);
            realtimeEventPublisher.publishRoomUpdatedAfterCommit(saved);
            return saved;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }
}
