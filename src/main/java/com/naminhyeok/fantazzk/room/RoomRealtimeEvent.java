package com.naminhyeok.fantazzk.room;

import java.time.Instant;

sealed interface RoomRealtimeEvent permits
    RoomUpdatedEvent,
    GameUpdatedEvent {

    String eventType();

    String roomCode();

    long snapshotVersion();

    Instant publishedAt();
}
