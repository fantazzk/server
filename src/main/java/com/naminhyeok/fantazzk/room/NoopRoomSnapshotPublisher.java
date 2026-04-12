package com.naminhyeok.fantazzk.room;

class NoopRoomSnapshotPublisher implements RoomSnapshotPublisher {
    @Override
    public void publishAfterCommit(Room room) {
    }
}
