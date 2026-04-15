package com.naminhyeok.fantazzk.room;

interface RoomSnapshotPublisher {
    void publishAfterCommit(Room room);

    default void publishAfterCommit(RoomDetails details) {
        if (details.game() != null) {
            throw new IllegalStateException("started room snapshot must preserve game state");
        }
        publishAfterCommit(details.room());
    }
}
