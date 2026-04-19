package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import java.time.Instant;

public sealed interface RoomRealtimeEvent permits
    RoomUpdatedEvent,
    GameUpdatedEvent {

    public String eventType();

    public String roomCode();

    public long snapshotVersion();

    public Instant publishedAt();
}
