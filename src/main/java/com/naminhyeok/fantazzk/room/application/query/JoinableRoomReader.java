package com.naminhyeok.fantazzk.room.application.query;

import java.util.List;

public interface JoinableRoomReader {
    List<JoinableRoomSummary> findLatestWaitingRooms(int limit);
}
