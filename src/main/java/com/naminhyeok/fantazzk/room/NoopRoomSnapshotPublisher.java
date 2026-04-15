package com.naminhyeok.fantazzk.room;

class NoopRoomSnapshotPublisher implements RoomSnapshotPublisher {
    @Override
    public void publishAfterCommit(Room room) {
    }

    @Override
    public void publishAfterCommit(RoomDetails details) {
    }
}
