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
                snapshot.participants().stream().map(AuctionParticipant.class::cast).toList(),
                snapshot.playerPool(),
                1,
                snapshot.startedAt().plusSeconds(snapshot.rules().auctionRules().pickBanTime())
            );
            case DRAFT -> new DraftGame(
                snapshot.gameId(),
                snapshot.roomId(),
                snapshot.roomCode(),
                snapshot.startedAt(),
                snapshot.rules(),
                snapshot.participants().stream().map(DraftParticipant.class::cast).toList(),
                snapshot.playerPool(),
                0
            );
        };
    }
}
