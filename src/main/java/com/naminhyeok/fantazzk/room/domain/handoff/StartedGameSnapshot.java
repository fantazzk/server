package com.naminhyeok.fantazzk.room.domain.handoff;

import com.naminhyeok.fantazzk.room.domain.room.RoomId;
import com.naminhyeok.fantazzk.room.domain.room.RoomMode;
import com.naminhyeok.fantazzk.room.domain.shared.GameId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record StartedGameSnapshot(
    RoomId roomId,
    String roomCode,
    GameId gameId,
    Instant startedAt,
    RoomMode gameMode,
    GameRules rules,
    List<? extends StartedGameParticipant> participants,
    List<StartedGamePlayer> playerPool
) {
    public StartedGameSnapshot {
        Objects.requireNonNull(roomId, "roomId must not be null");
        Objects.requireNonNull(roomCode, "roomCode must not be null");
        Objects.requireNonNull(gameId, "gameId must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        Objects.requireNonNull(gameMode, "gameMode must not be null");
        Objects.requireNonNull(rules, "rules must not be null");
        participants = List.copyOf(participants);
        playerPool = List.copyOf(playerPool);
    }
}
