package com.naminhyeok.fantazzk.room.application.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.RoomView;
import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.application.support.RoomActionAuthorizer;
import com.naminhyeok.fantazzk.room.application.support.RoomSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClearDraftPosition {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final RoomSnapshotPublisher roomSnapshotPublisher;

    @Transactional
    public Room clear(String code, String actionToken) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            room.clearDraftPosition(caller.getId());
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(new RoomSnapshot(saved.getCode(), saved.getVersion(), RoomView.from(saved)));
            return saved;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }
}
