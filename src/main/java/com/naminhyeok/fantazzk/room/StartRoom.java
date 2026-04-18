package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;
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
class StartRoom {
    private final Rooms rooms;
    private final Games games;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final GameFactory gameFactory;
    private final RoomSnapshotPublisher roomSnapshotPublisher;
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
            roomSnapshotPublisher.publishAfterCommit(
                new StartedRoomSnapshot(saved.getCode(), saved.getVersion() + createdGame.getVersion(), GameView.from(createdGame))
            );
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
