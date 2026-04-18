package com.naminhyeok.fantazzk.room.domain.handoff;

import com.naminhyeok.fantazzk.room.domain.shared.GameId;
import com.naminhyeok.fantazzk.room.domain.shared.RoomId;
import com.naminhyeok.fantazzk.room.domain.shared.RoomMode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record StartedGameSnapshot(
    RoomId roomId,
    String roomCode,
    GameId gameId,
    Instant startedAt,
    GameRules rules,
    List<? extends StartedGameParticipant> participants,
    List<StartedGamePlayer> playerPool
) {
    public StartedGameSnapshot {
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(roomCode, "roomCode must not be null");
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        Objects.requireNonNull(rules, "rules must not be null");
        participants = List.copyOf(participants);
        playerPool = List.copyOf(playerPool);

        validateParticipants(rules.mode(), participants);
    }

    public List<StartedAuctionParticipant> auctionParticipants() {
        requireMode(RoomMode.AUCTION);
        return participants.stream().map(StartedAuctionParticipant.class::cast).toList();
    }

    public List<StartedDraftParticipant> draftParticipants() {
        requireMode(RoomMode.DRAFT);
        return participants.stream().map(StartedDraftParticipant.class::cast).toList();
    }

    private void requireMode(RoomMode expectedMode) {
        if (rules.mode() != expectedMode) {
            throw new IllegalStateException("rules mode must be " + expectedMode);
        }
    }

    private static void validateParticipants(
        RoomMode mode,
        List<? extends StartedGameParticipant> participants
    ) {
        boolean invalidParticipantType = switch (mode) {
            case AUCTION -> participants.stream().anyMatch(participant -> !(participant instanceof StartedAuctionParticipant));
            case DRAFT -> participants.stream().anyMatch(participant -> !(participant instanceof StartedDraftParticipant));
        };
        if (invalidParticipantType) {
            throw new IllegalArgumentException("handoff participant types must match rules mode: " + mode);
        }
    }
}
