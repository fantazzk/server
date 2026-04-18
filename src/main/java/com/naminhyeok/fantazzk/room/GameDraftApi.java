package com.naminhyeok.fantazzk.room;

import java.util.UUID;
import org.springframework.stereotype.Service;

public interface GameDraftApi {
    GameView pick(UUID gameId, String actionToken, String playerName);
}

@Service
class ProvideGameDraftApi implements GameDraftApi {
    private final PickDraft pickDraft;
    private final GetGame getGame;

    ProvideGameDraftApi(PickDraft pickDraft, GetGame getGame) {
        this.pickDraft = pickDraft;
        this.getGame = getGame;
    }

    @Override
    public GameView pick(UUID gameId, String actionToken, String playerName) {
        pickDraft.pick(gameId, actionToken, playerName);
        return GameView.from(getGame.get(gameId));
    }
}
