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

    static RealtimeSnapshotEvent from(RoomDetails details, Instant publishedAt) {
        if (details.game() != null) {
            return new RealtimeSnapshotEvent(
                GAME_SNAPSHOT_UPDATED,
                details.room().getCode(),
                snapshotVersionOf(details),
                publishedAt,
                null,
                GameResponse.from(details.game())
            );
        }
        return new RealtimeSnapshotEvent(
            ROOM_SNAPSHOT_UPDATED,
            details.room().getCode(),
            snapshotVersionOf(details),
            publishedAt,
            RoomViewResponse.from(details.room()),
            null
        );
    }

    static RealtimeSnapshotEvent from(Room room, Instant publishedAt) {
        return from(RoomDetails.from(room), publishedAt);
    }

    static long snapshotVersionOf(RoomDetails details) {
        if (details.game() != null) {
            return details.room().getVersion() + details.game().getVersion();
        }
        return details.room().getVersion();
    }
}
