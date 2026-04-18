package com.naminhyeok.fantazzk.room;

import java.time.Instant;

sealed interface RealtimeSnapshotEvent permits RoomSnapshotUpdatedEvent, GameSnapshotUpdatedEvent {
    String eventType();

    String roomCode();

    long snapshotVersion();

    Instant publishedAt();

    default RoomView room() {
        return null;
    }

    default GameView game() {
        return null;
    }

    static RealtimeSnapshotEvent from(StartedRoomSnapshot snapshot, Instant publishedAt) {
        return new GameSnapshotUpdatedEvent(
            "GAME_SNAPSHOT_UPDATED",
            snapshot.room().getCode(),
            snapshotVersionOf(snapshot),
            publishedAt,
            GameView.from(snapshot.game())
        );
    }

    static RealtimeSnapshotEvent from(Room room, Instant publishedAt) {
        return new RoomSnapshotUpdatedEvent(
            "ROOM_SNAPSHOT_UPDATED",
            room.getCode(),
            snapshotVersionOf(room),
            publishedAt,
            RoomView.from(room)
        );
    }

    static long snapshotVersionOf(StartedRoomSnapshot snapshot) {
        return snapshot.room().getVersion() + snapshot.game().getVersion();
    }

    static long snapshotVersionOf(Room room) {
        return room.getVersion();
    }
}
