package com.naminhyeok.fantazzk.room;

import java.time.Instant;

final class RoomRealtimeEventFactory {
    private RoomRealtimeEventFactory() {
    }

    static RoomRealtimeEvent roomMembershipUpdated(Room room, Instant publishedAt) {
        return new RoomMembershipUpdatedEvent(
            "ROOM_MEMBERSHIP_UPDATED",
            room.getCode(),
            snapshotVersionOf(room),
            publishedAt,
            RoomMembershipProjection.from(room)
        );
    }

    static RoomRealtimeEvent roomDraftOrderUpdated(Room room, Instant publishedAt) {
        return new RoomDraftOrderUpdatedEvent(
            "ROOM_DRAFT_ORDER_UPDATED",
            room.getCode(),
            snapshotVersionOf(room),
            publishedAt,
            RoomDraftOrderProjection.from(room)
        );
    }

    static RoomRealtimeEvent gameStarted(StartedRoomSnapshot snapshot, Instant publishedAt) {
        return new GameStartedEvent(
            "GAME_STARTED",
            snapshot.room().getCode(),
            snapshotVersionOf(snapshot),
            publishedAt,
            snapshot.game().getId().gameId().toString(),
            GameStartProjection.from(snapshot.game())
        );
    }

    static RoomRealtimeEvent gameAuctionProgressUpdated(StartedRoomSnapshot snapshot, Instant publishedAt) {
        return new GameAuctionProgressUpdatedEvent(
            "GAME_AUCTION_PROGRESS_UPDATED",
            snapshot.room().getCode(),
            snapshotVersionOf(snapshot),
            publishedAt,
            snapshot.game().getId().gameId().toString(),
            AuctionProgressResponse.from(snapshot.game())
        );
    }

    static RoomRealtimeEvent gameDraftProgressUpdated(StartedRoomSnapshot snapshot, Instant publishedAt) {
        return new GameDraftProgressUpdatedEvent(
            "GAME_DRAFT_PROGRESS_UPDATED",
            snapshot.room().getCode(),
            snapshotVersionOf(snapshot),
            publishedAt,
            snapshot.game().getId().gameId().toString(),
            DraftProgressResponse.from(snapshot.game())
        );
    }

    static RoomRealtimeEvent gameRosterUpdated(StartedRoomSnapshot snapshot, Instant publishedAt) {
        return new GameRosterUpdatedEvent(
            "GAME_ROSTER_UPDATED",
            snapshot.room().getCode(),
            snapshotVersionOf(snapshot),
            publishedAt,
            snapshot.game().getId().gameId().toString(),
            GameRosterProjection.from(snapshot.game())
        );
    }

    static long snapshotVersionOf(StartedRoomSnapshot snapshot) {
        return snapshot.room().getVersion() + snapshot.game().getVersion();
    }

    static long snapshotVersionOf(Room room) {
        return room.getVersion();
    }
}
