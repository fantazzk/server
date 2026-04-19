package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record RoomMembershipUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    RoomMembershipProjection membership
) implements RoomRealtimeEvent {
}
