package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.room.application.game.GetGame;
import com.naminhyeok.fantazzk.room.application.game.PickDraft;
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
