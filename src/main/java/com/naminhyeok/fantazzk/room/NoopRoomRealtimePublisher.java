package com.naminhyeok.fantazzk.room;

class NoopRoomRealtimePublisher implements RoomRealtimePublisher {
    @Override
    public void publishAfterCommit(Room room) {
    }
}
