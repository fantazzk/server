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

    static RoomRealtimeSnapshotEvent from(Room room, Instant publishedAt) {
        return new RoomRealtimeSnapshotEvent(
            ROOM_SNAPSHOT_UPDATED,
            room.getCode(),
            room.getVersion(),
            publishedAt,
            RoomResponse.from(room)
        );
    }
}
