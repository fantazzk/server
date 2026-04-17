package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.draft.DraftRoomPlay;
import com.naminhyeok.fantazzk.draft.DraftRoomState;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.jmolecules.ddd.annotation.Service
class PickDraft {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final RoomSnapshotPublisher roomSnapshotPublisher;
    private final DraftRoomPlay draftRoomPlay;

    PickDraft(
        Rooms rooms,
        RoomActionAuthorizer roomActionAuthorizer,
        RoomSnapshotPublisher roomSnapshotPublisher,
        DraftRoomPlay draftRoomPlay
    ) {
        this.rooms = rooms;
        this.roomActionAuthorizer = roomActionAuthorizer;
        this.roomSnapshotPublisher = roomSnapshotPublisher;
        this.draftRoomPlay = draftRoomPlay;
    }

    @Transactional
    public Room pick(String code, String actionToken, RoomPlayerId playerId) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            if (draftRoomPlay != null) {
                try {
                    DraftRoomState draftState = draftRoomPlay.pick(code, caller.getId().value(), playerId.value());
                    room.applyDraftState(draftState);
                } catch (RuntimeException ex) {
                    room.pick(caller.getId(), playerId);
                }
            } else {
                room.pick(caller.getId(), playerId);
            }
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(saved);
            return saved;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }
}
