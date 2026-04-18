package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import java.util.UUID;
import org.springframework.stereotype.Service;

public interface GameAuctionApi {
    GameView placeBid(UUID gameId, String actionToken, int amount);

    GameView settleIfDue(UUID gameId);
}

@Service
class ProvideGameAuctionApi implements GameAuctionApi {
    private final PlaceBid placeBid;
    private final SettleAuction settleAuction;
    private final GetGame getGame;

    ProvideGameAuctionApi(PlaceBid placeBid, SettleAuction settleAuction, GetGame getGame) {
        this.placeBid = placeBid;
        this.settleAuction = settleAuction;
        this.getGame = getGame;
    }

    @Override
    public GameView placeBid(UUID gameId, String actionToken, int amount) {
        placeBid.place(gameId, actionToken, amount);
        return GameView.from(getGame.get(gameId));
    }

    @Override
    public GameView settleIfDue(UUID gameId) {
        return GameView.from(settleAuction.settleIfDue(gameId));
    }
}
