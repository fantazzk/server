package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartedGameContextLoader {
    private final Rooms rooms;
    private final Games games;
    private final RoomActionAuthorizer roomActionAuthorizer;

    public StartedGameContext load(UUID gameId) {
        Game game = games.findById(new GameId(gameId)).orElseThrow(() -> CoreException.of(RoomErrorType.GAME_NOT_FOUND));
        Room room = rooms.findById(game.getRoomId()).orElseThrow(() -> CoreException.of(RoomErrorType.GAME_NOT_FOUND));
        return new StartedGameContext(room, game);
    }

    public StartedGameActionContext authenticate(UUID gameId, String actionToken) {
        StartedGameContext context = load(gameId);
        RoomTeamLeader caller = roomActionAuthorizer.authenticate(context.room(), actionToken);
        return new StartedGameActionContext(context.room(), context.game(), caller);
    }
}
