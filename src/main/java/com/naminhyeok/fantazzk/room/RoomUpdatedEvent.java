package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record RoomUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    RoomDetailResponse room
) implements RoomRealtimeEvent {
}
