package com.naminhyeok.fantazzk.room;

import java.time.Instant;

sealed interface RoomRealtimeEvent permits
    RoomMembershipUpdatedEvent,
    RoomDraftOrderUpdatedEvent,
    GameStartedEvent,
    GameAuctionProgressUpdatedEvent,
    GameDraftProgressUpdatedEvent,
    GameRosterUpdatedEvent {

    String eventType();

    String roomCode();

    long snapshotVersion();

    Instant publishedAt();
}
