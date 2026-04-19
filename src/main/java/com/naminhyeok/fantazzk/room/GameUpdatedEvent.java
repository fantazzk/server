package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record GameUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    GameDetailResponse game
) implements RoomRealtimeEvent {
}
