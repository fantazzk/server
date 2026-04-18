package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;
import java.util.UUID;
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
    private final StartedGameContextLoader startedGameContextLoader;
    private final RoomSnapshotPublisher roomSnapshotPublisher;

    @Transactional
    public RosterMember pick(String code, String actionToken, String playerName) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            DraftGame game = requireDraftGame(room);
            RosterMember member = game.pick(caller.getId(), playerName);
            games.save(game);
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(
                new StartedRoomSnapshot(saved.getCode(), saved.getVersion() + game.getVersion(), GameView.from(game))
            );
            return member;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    @Transactional
    public RosterMember pick(UUID gameId, String actionToken, String playerName) {
        try {
            StartedGameActionContext action = startedGameContextLoader.authenticate(gameId, actionToken);
            DraftGame draftGame = requireDraftGame(action.game());
            RosterMember member = draftGame.pick(action.caller().getId(), playerName);
            games.save(draftGame);
            Room saved = rooms.saveAndFlush(action.room());
            roomSnapshotPublisher.publishAfterCommit(
                new StartedRoomSnapshot(saved.getCode(), saved.getVersion() + draftGame.getVersion(), GameView.from(draftGame))
            );
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
        if (room.getStartedGameId() == null) {
            throw RoomStateInvalidException.draftTurnMissing();
        }
        Game game = games.findById(room.getStartedGameId()).orElseThrow(RoomStateInvalidException::draftTurnMissing);
        if (!(game instanceof DraftGame draftGame)) {
            throw RoomStateInvalidException.draftTurnMissing();
        }
        return draftGame;
    }

    private DraftGame requireDraftGame(Game game) {
        if (!(game instanceof DraftGame draftGame)) {
            throw CoreException.of(RoomErrorType.ROOM_PICK_REQUIRES_DRAFT_MODE);
        }
        return draftGame;
    }
}
