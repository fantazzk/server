package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import com.naminhyeok.fantazzk.room.application.RoomRealtimeEventPublisher;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;

public class NoopRoomRealtimeEventPublisher implements RoomRealtimeEventPublisher {
    @Override
    public void publishRoomUpdatedAfterCommit(Room room) {
    }

    @Override
    public void publishGameUpdatedAfterCommit(StartedRoomSnapshot snapshot) {
    }
}
