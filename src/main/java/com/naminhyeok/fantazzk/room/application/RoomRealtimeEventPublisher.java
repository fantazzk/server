package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;

public interface RoomRealtimeEventPublisher {
    public void publishRoomUpdatedAfterCommit(Room room);

    public void publishGameUpdatedAfterCommit(StartedRoomSnapshot snapshot);
}
