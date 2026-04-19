package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record RoomDraftOrderUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    RoomDraftOrderProjection draftOrder
) implements RoomRealtimeEvent {
}
