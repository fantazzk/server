package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class StartedGameContextLoader {
    private final Rooms rooms;
    private final Games games;
    private final RoomActionAuthorizer roomActionAuthorizer;

    StartedGameContext load(UUID gameId) {
        Game game = games.findById(new GameId(gameId)).orElseThrow(() -> CoreException.of(RoomErrorType.GAME_NOT_FOUND));
        Room room = rooms.findById(game.getRoomId()).orElseThrow(() -> CoreException.of(RoomErrorType.GAME_NOT_FOUND));
        return new StartedGameContext(room, game);
    }

    StartedGameActionContext authenticate(UUID gameId, String actionToken) {
        StartedGameContext context = load(gameId);
        RoomTeamLeader caller = roomActionAuthorizer.authenticate(context.room(), actionToken);
        return new StartedGameActionContext(context.room(), context.game(), caller);
    }
}

record StartedGameContext(
    Room room,
    Game game
) {
    StartedGameContext {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(game, "game must not be null");
    }
}

record StartedGameActionContext(
    Room room,
    Game game,
    RoomTeamLeader caller
) {
    StartedGameActionContext {
        Objects.requireNonNull(room, "room must not be null");
        Objects.requireNonNull(game, "game must not be null");
        Objects.requireNonNull(caller, "caller must not be null");
    }
}
