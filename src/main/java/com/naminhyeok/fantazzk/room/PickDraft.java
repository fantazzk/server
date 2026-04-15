package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PickDraft {
    private final Rooms rooms;
    private final Games games;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final RoomSnapshotPublisher roomSnapshotPublisher;

    @Transactional
    public RoomTeamMember pick(String code, String actionToken, String playerName) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            DraftGame game = requireDraftGame(room);
            RoomTeamMember member = game.pick(caller.getId(), playerName);
            games.save(game);
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(new RoomDetails(saved, game));
            return member;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    private DraftGame requireDraftGame(Room room) {
        if (room.getMode() != RoomMode.DRAFT) {
            throw CoreException.of(RoomErrorType.ROOM_PICK_REQUIRES_DRAFT_MODE);
        }
        if (room.getStatus() != RoomStatus.STARTED) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        if (games == null || room.getStartedGameId() == null) {
            throw RoomStateInvalidException.draftTurnMissing();
        }
        Game game = games.findById(room.getStartedGameId()).orElseThrow(RoomStateInvalidException::draftTurnMissing);
        if (!(game instanceof DraftGame draftGame)) {
            throw RoomStateInvalidException.draftTurnMissing();
        }
        return draftGame;
    }
}
