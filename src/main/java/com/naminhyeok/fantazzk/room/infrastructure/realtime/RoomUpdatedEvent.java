package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import com.naminhyeok.fantazzk.room.query.RoomDetailResponse;
import java.time.Instant;

public record RoomUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    RoomDetailResponse room
) implements RoomRealtimeEvent {
}
