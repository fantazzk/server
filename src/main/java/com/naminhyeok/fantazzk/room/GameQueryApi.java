package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.room.application.game.GetGame;
import java.util.UUID;
import org.springframework.stereotype.Service;

public interface GameQueryApi {
    GameView get(UUID gameId);
}

@Service
class ProvideGameQueryApi implements GameQueryApi {
    private final GetGame getGame;

    ProvideGameQueryApi(GetGame getGame) {
        this.getGame = getGame;
    }

    @Override
    public GameView get(UUID gameId) {
        return GameView.from(getGame.get(gameId));
    }
}
