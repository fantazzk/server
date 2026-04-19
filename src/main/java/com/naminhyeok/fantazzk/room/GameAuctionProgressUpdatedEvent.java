package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record GameAuctionProgressUpdatedEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    String gameId,
    AuctionProgressResponse auctionProgress
) implements RoomRealtimeEvent {
}
