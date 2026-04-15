package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record RoomRealtimeSnapshotEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    RoomResponse room
) {
    private static final String ROOM_SNAPSHOT_UPDATED = "ROOM_SNAPSHOT_UPDATED";

    static RoomRealtimeSnapshotEvent from(RoomDetails details, Instant publishedAt) {
        return new RoomRealtimeSnapshotEvent(
            ROOM_SNAPSHOT_UPDATED,
            details.room().getCode(),
            snapshotVersionOf(details),
            publishedAt,
            RoomResponse.from(details)
        );
    }

    static RoomRealtimeSnapshotEvent from(Room room, Instant publishedAt) {
        return from(RoomDetails.from(room), publishedAt);
    }

    static long snapshotVersionOf(RoomDetails details) {
        if (details.game() != null) {
            return details.room().getVersion() + details.game().getVersion();
        }
        return details.room().getVersion();
    }
}
