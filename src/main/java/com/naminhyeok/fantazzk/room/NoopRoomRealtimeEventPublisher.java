package com.naminhyeok.fantazzk.room;

class NoopRoomRealtimeEventPublisher implements RoomRealtimeEventPublisher {
    @Override
    public void publishRoomUpdatedAfterCommit(Room room) {
    }

    @Override
    public void publishGameUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
    }
}
