package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record RoomSnapshotUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    RoomViewResponse room
) implements RealtimeSnapshotEvent {
}
