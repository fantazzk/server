package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import com.naminhyeok.fantazzk.room.RoomView;
import java.time.Instant;

public record RoomSnapshotUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    RoomView room
) implements RealtimeSnapshotEvent {
}
