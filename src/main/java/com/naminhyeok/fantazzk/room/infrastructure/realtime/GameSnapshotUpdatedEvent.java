package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import com.naminhyeok.fantazzk.room.GameView;
import java.time.Instant;

public record GameSnapshotUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    GameView game
) implements RealtimeSnapshotEvent {
}
