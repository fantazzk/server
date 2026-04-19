package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import com.naminhyeok.fantazzk.room.query.GameDetailResponse;
import java.time.Instant;

public record GameUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    GameDetailResponse game
) implements RoomRealtimeEvent {
}
