package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record GameRosterUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    String gameId,
    GameRosterProjection roster
) implements RoomRealtimeEvent {
}
