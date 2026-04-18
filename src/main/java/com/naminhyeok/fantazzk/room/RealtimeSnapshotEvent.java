package com.naminhyeok.fantazzk.room;

import java.time.Instant;

record RealtimeSnapshotEvent(
    String eventType,
    String roomCode,
    long snapshotVersion,
    Instant publishedAt,
    RoomViewResponse room,
    GameResponse game
) {
    private static final String ROOM_SNAPSHOT_UPDATED = "ROOM_SNAPSHOT_UPDATED";
    private static final String GAME_SNAPSHOT_UPDATED = "GAME_SNAPSHOT_UPDATED";

    static RealtimeSnapshotEvent from(StartedRoomSnapshot snapshot, Instant publishedAt) {
        return new RealtimeSnapshotEvent(
            GAME_SNAPSHOT_UPDATED,
            snapshot.room().getCode(),
            snapshotVersionOf(snapshot),
            publishedAt,
            null,
            GameResponse.from(snapshot.game())
        );
    }

    static RealtimeSnapshotEvent from(Room room, Instant publishedAt) {
        return new RealtimeSnapshotEvent(
            ROOM_SNAPSHOT_UPDATED,
            room.getCode(),
            snapshotVersionOf(room),
            publishedAt,
            RoomViewResponse.from(room),
            null
        );
    }

    static long snapshotVersionOf(StartedRoomSnapshot snapshot) {
        return snapshot.room().getVersion() + snapshot.game().getVersion();
    }

    static long snapshotVersionOf(Room room) {
        return room.getVersion();
    }
}
