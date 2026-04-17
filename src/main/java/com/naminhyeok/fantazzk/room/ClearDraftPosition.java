package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.draft.DraftRoomLifecycle;
import com.naminhyeok.fantazzk.draft.DraftRoomState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ClearDraftPosition {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final RoomSnapshotPublisher roomSnapshotPublisher;
    private final DraftRoomLifecycle draftRoomLifecycle;

    @Autowired
    ClearDraftPosition(
        Rooms rooms,
        RoomActionAuthorizer roomActionAuthorizer,
        RoomSnapshotPublisher roomSnapshotPublisher,
        DraftRoomLifecycle draftRoomLifecycle
    ) {
        this.rooms = rooms;
        this.roomActionAuthorizer = roomActionAuthorizer;
        this.roomSnapshotPublisher = roomSnapshotPublisher;
        this.draftRoomLifecycle = draftRoomLifecycle;
    }

    ClearDraftPosition(Rooms rooms, RoomActionAuthorizer roomActionAuthorizer, RoomSnapshotPublisher roomSnapshotPublisher) {
        this(rooms, roomActionAuthorizer, roomSnapshotPublisher, null);
    }

    @Transactional
    public Room clear(String code, String actionToken) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            if (draftRoomLifecycle != null) {
                try {
                    DraftRoomState draftState = draftRoomLifecycle.clearDraftPosition(code, caller.getId().value());
                    room.applyDraftState(draftState);
                } catch (RuntimeException ex) {
                    room.clearDraftPosition(caller.getId());
                }
            } else {
                room.clearDraftPosition(caller.getId());
            }
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(saved);
            return saved;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }
}
