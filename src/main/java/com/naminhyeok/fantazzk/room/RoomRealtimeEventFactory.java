package com.naminhyeok.fantazzk.room;

import java.time.Instant;

final class RoomRealtimeEventFactory {
    private RoomRealtimeEventFactory() {
    }

    static RoomRealtimeEvent roomUpdated(Room room, Instant publishedAt) {
        return new RoomUpdatedEvent(
            "ROOM_UPDATED",
            room.getCode(),
            snapshotVersionOf(room),
            publishedAt,
            RoomDetailResponse.from(room)
        );
    }

    static RoomRealtimeEvent gameUpdated(StartedRoomSnapshot snapshot, Instant publishedAt) {
        return new GameUpdatedEvent(
            "GAME_UPDATED",
            snapshot.room().getCode(),
            snapshotVersionOf(snapshot),
            publishedAt,
            GameDetailResponse.from(snapshot.game())
        );
    }

    static long snapshotVersionOf(StartedRoomSnapshot snapshot) {
        return snapshot.room().getVersion() + snapshot.game().getVersion();
    }

    static long snapshotVersionOf(Room room) {
        return room.getVersion();
    }
}
