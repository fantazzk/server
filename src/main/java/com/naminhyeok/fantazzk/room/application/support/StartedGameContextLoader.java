package com.naminhyeok.fantazzk.room.application.support;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.CoreException;
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
