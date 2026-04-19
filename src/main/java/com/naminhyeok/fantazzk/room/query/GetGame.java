package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.GameId;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.repository.Games;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetGame {
    private final Games games;

    @Transactional(readOnly = true)
    public Game get(UUID gameId) {
        return games.findById(new GameId(gameId)).orElseThrow(() -> CoreException.of(RoomErrorType.GAME_NOT_FOUND));
    }
}
