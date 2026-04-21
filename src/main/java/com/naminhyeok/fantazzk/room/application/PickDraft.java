package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.DraftGame;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
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
    private final StartedGameContextLoader startedGameContextLoader;
    private final RoomRealtimeEventPublisher realtimeEventPublisher;

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

    private DraftGame requireDraftGame(Game game) {
        if (!(game instanceof DraftGame draftGame)) {
            throw CoreException.of(RoomErrorType.ROOM_PICK_REQUIRES_DRAFT_MODE);
        }
        return draftGame;
    }
}
