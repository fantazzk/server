package com.naminhyeok.fantazzk.room.infrastructure.realtime;

import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.application.support.RoomSnapshot;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;

public class NoopRoomSnapshotPublisher implements RoomSnapshotPublisher {
    @Override
    public void publishAfterCommit(RoomSnapshot snapshot) {
    }

    @Override
    public void publishAfterCommit(StartedRoomSnapshot snapshot) {
    }
}
