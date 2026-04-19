package com.naminhyeok.fantazzk.room;

interface RoomRealtimeEventPublisher {
    void publishRoomMembershipUpdatedAfterCommit(Room room);

    void publishRoomDraftOrderUpdatedAfterCommit(Room room);

    void publishGameStartedAfterCommit(StartedRoomSnapshot snapshot);

    void publishGameAuctionProgressUpdatedAfterCommit(StartedRoomSnapshot snapshot);

    void publishGameDraftProgressUpdatedAfterCommit(StartedRoomSnapshot snapshot);

    void publishGameRosterUpdatedAfterCommit(StartedRoomSnapshot snapshot);
}
