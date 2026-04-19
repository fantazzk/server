package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.query.GameDetailResponse;
import com.naminhyeok.fantazzk.room.query.RoomDetailResponse;
import java.time.Instant;

public class RoomRealtimeEventFactory {
    private RoomRealtimeEventFactory() {
    }

    public static RoomRealtimeEvent roomUpdated(Room room, Instant publishedAt) {
        return new RoomUpdatedEvent(
            "ROOM_UPDATED",
            room.getCode(),
            snapshotVersionOf(room),
            publishedAt,
            RoomDetailResponse.from(room)
        );
    }

    public static RoomRealtimeEvent gameUpdated(StartedRoomSnapshot snapshot, Instant publishedAt) {
        return new GameUpdatedEvent(
            "GAME_UPDATED",
            snapshot.room().getCode(),
            snapshotVersionOf(snapshot),
            publishedAt,
            GameDetailResponse.from(snapshot.game())
        );
    }

    public static long snapshotVersionOf(StartedRoomSnapshot snapshot) {
        return snapshot.room().getVersion() + snapshot.game().getVersion();
    }

    public static long snapshotVersionOf(Room room) {
        return room.getVersion();
    }
}
