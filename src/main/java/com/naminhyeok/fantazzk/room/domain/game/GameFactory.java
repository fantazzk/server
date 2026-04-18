package com.naminhyeok.fantazzk.room.domain.game;

import com.naminhyeok.fantazzk.room.domain.event.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.room.domain.handoff.StartedAuctionParticipant;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedDraftParticipant;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedGamePlayer;
import com.naminhyeok.fantazzk.room.domain.handoff.StartedGameSnapshot;
import org.springframework.stereotype.Component;

@Component
public class GameFactory {
    public Game create(StartedGameSnapshot snapshot) {
        return switch (snapshot.rules().mode()) {
            case AUCTION -> new AuctionGame(
                snapshot.gameId(),
                snapshot.roomId(),
                snapshot.roomCode(),
                snapshot.startedAt(),
                snapshot.rules(),
                snapshot.auctionParticipants().stream()
                    .map(participant -> new AuctionParticipant(
                        participant.teamLeaderId(),
                        participant.nickname(),
                        participant.remainingBudget()
                    ))
                    .toList(),
                snapshot.playerPool().stream().map(GameFactory::toGamePlayer).toList(),
                1,
                snapshot.startedAt().plusSeconds(snapshot.rules().auctionRules().pickBanTime())
            );
            case DRAFT -> new DraftGame(
                snapshot.gameId(),
                snapshot.roomId(),
                snapshot.roomCode(),
                snapshot.startedAt(),
                snapshot.rules(),
                snapshot.draftParticipants().stream()
                    .map(participant -> new DraftParticipant(
                        participant.teamLeaderId(),
                        participant.nickname(),
                        participant.draftPosition()
                    ))
                    .toList(),
                snapshot.playerPool().stream().map(GameFactory::toGamePlayer).toList(),
                0
            );
        };
    }

    private static GamePlayer toGamePlayer(StartedGamePlayer player) {
        return new GamePlayer(player.playerId(), player.name(), player.position(), player.displayOrder());
    }
}
