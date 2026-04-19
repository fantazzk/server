package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record GameDraftProgressUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    String gameId,
    DraftProgressResponse draftProgress
) implements RoomRealtimeEvent {
}
