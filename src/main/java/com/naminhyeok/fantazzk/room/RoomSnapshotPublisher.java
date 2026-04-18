package com.naminhyeok.fantazzk.room;

interface RoomSnapshotPublisher {
    void publishAfterCommit(Room room);
    void publishAfterCommit(StartedRoomSnapshot snapshot);
}
