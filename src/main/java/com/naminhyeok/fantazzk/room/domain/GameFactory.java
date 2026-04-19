package com.naminhyeok.fantazzk.room.domain;

import org.springframework.stereotype.Component;

@Component
public class GameFactory {
    public Game create(StartedGameSnapshot snapshot) {
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
