package com.naminhyeok.fantazzk.room.application.port;

import com.naminhyeok.fantazzk.room.application.support.RoomSnapshot;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;

public interface RoomSnapshotPublisher {
    void publishAfterCommit(RoomSnapshot snapshot);
    void publishAfterCommit(StartedRoomSnapshot snapshot);
}
