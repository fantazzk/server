package com.naminhyeok.fantazzk.room;

import org.springframework.stereotype.Component;

@Component
class GameFactory {
    Game create(StartedGameSnapshot snapshot) {
        return switch (snapshot.gameMode()) {
            case AUCTION -> new AuctionGame(
                snapshot.gameId(),
                snapshot.roomId(),
                snapshot.roomCode(),
                snapshot.startedAt(),
                snapshot.rules(),
                snapshot.participants(),
                snapshot.playerPool(),
                1,
                snapshot.startedAt().plusSeconds(snapshot.rules().pickBanTime())
            );
            case DRAFT -> new DraftGame(
                snapshot.gameId(),
                snapshot.roomId(),
                snapshot.roomCode(),
                snapshot.startedAt(),
                snapshot.rules(),
                snapshot.participants(),
                snapshot.playerPool(),
                0
            );
        };
    }
}
