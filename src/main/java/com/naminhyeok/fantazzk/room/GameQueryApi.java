package com.naminhyeok.fantazzk.room;

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
