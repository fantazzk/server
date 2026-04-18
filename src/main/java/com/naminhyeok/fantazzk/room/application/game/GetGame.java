package com.naminhyeok.fantazzk.room.application.game;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.CoreException;
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
