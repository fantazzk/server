package com.naminhyeok.fantazzk.room;

interface RoomSnapshotPublisher {
    void publishAfterCommit(Room room);
}
