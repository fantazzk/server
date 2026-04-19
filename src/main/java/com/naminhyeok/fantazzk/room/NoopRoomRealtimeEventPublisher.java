package com.naminhyeok.fantazzk.room;

class NoopRoomRealtimeEventPublisher implements RoomRealtimeEventPublisher {
    @Override
    public void publishRoomMembershipUpdatedAfterCommit(Room room) {
    }

    @Override
    public void publishRoomDraftOrderUpdatedAfterCommit(Room room) {
    }

    @Override
    public void publishGameStartedAfterCommit(StartedRoomSnapshot snapshot) {
    }

    @Override
    public void publishGameAuctionProgressUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
    }

    @Override
    public void publishGameDraftProgressUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
    }

    @Override
    public void publishGameRosterUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
    }
}
