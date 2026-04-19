package com.naminhyeok.fantazzk.room;

interface RoomRealtimeEventPublisher {
    void publishRoomUpdatedAfterCommit(Room room);

    void publishGameUpdatedAfterCommit(StartedRoomSnapshot snapshot);
}
