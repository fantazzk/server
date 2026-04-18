package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record GameSnapshotUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    GameResponse game
) implements RealtimeSnapshotEvent {
}
