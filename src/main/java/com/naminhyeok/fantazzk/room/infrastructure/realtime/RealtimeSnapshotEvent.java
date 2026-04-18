package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import com.naminhyeok.fantazzk.room.GameView;
import com.naminhyeok.fantazzk.room.RoomView;
import com.naminhyeok.fantazzk.room.application.support.RoomSnapshot;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;
import java.time.Instant;

public sealed interface RealtimeSnapshotEvent permits RoomSnapshotUpdatedEvent, GameSnapshotUpdatedEvent {
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
            snapshot.roomCode(),
            snapshot.snapshotVersion(),
            publishedAt,
            snapshot.game()
        );
    }

    static RealtimeSnapshotEvent from(RoomSnapshot snapshot, Instant publishedAt) {
        return new RoomSnapshotUpdatedEvent(
            "ROOM_SNAPSHOT_UPDATED",
            snapshot.roomCode(),
            snapshot.snapshotVersion(),
            publishedAt,
            snapshot.room()
        );
    }
}
