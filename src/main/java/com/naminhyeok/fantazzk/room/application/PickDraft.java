package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.DraftGame;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomStateInvalidException;
import com.naminhyeok.fantazzk.room.domain.RoomStatus;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.RosterMember;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PickDraft {
    private final Rooms rooms;
    private final Games games;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final StartedGameContextLoader startedGameContextLoader;
    private final RoomRealtimeEventPublisher realtimeEventPublisher;

    @Transactional
    public RosterMember pick(String code, String actionToken, String playerName) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            DraftGame game = requireDraftGame(room);
            RosterMember member = game.pick(caller.getId(), playerName);
            games.save(game);
            Room saved = rooms.saveAndFlush(room);
            StartedRoomSnapshot snapshot = new StartedRoomSnapshot(saved, game);
            realtimeEventPublisher.publishGameUpdatedAfterCommit(snapshot);
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
            StartedRoomSnapshot snapshot = new StartedRoomSnapshot(saved, draftGame);
            realtimeEventPublisher.publishGameUpdatedAfterCommit(snapshot);
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
