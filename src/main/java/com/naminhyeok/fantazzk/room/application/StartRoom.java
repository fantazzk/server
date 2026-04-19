package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameFactory;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomId;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.domain.StartedGameSnapshot;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StartRoom {
    private final Rooms rooms;
    private final Games games;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final GameFactory gameFactory;
    private final RoomRealtimeEventPublisher realtimeEventPublisher;
    private final Clock clock;

    @Transactional
    public Game start(String code, String actionToken) {
        try {
            Room loaded = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(loaded, actionToken);
            Instant now = Instant.now(clock);
            GameId gameId = createDeterministicGameId(loaded.getId());
            StartedGameSnapshot startedGameSnapshot = loaded.start(caller.getId(), gameId, now);
            Game createdGame = gameFactory.create(startedGameSnapshot);
            games.save(createdGame);
            Room saved = rooms.saveAndFlush(loaded);
            StartedRoomSnapshot snapshot = new StartedRoomSnapshot(saved, createdGame);
            realtimeEventPublisher.publishRoomUpdatedAfterCommit(saved);
            realtimeEventPublisher.publishGameUpdatedAfterCommit(snapshot);
            return createdGame;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    private GameId createDeterministicGameId(RoomId roomId) {
        String source = "game:%s".formatted(roomId.roomId());
        return new GameId(UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8)));
    }
}
