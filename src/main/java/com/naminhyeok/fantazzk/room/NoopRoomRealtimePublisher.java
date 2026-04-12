package com.naminhyeok.fantazzk.room;

import org.springframework.stereotype.Service;

@Service
class NoopRoomRealtimePublisher implements RoomRealtimePublisher {
    @Override
    public void publishAfterCommit(Room room) {
    }
}
