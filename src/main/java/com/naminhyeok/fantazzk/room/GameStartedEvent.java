package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record GameStartedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    String gameId,
    GameStartProjection gameStart
) implements RoomRealtimeEvent {
}
