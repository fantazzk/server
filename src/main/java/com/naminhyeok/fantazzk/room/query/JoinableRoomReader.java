package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.Room;
import java.util.List;

public interface JoinableRoomReader {
    public List<Room> findLatestWaitingRooms(int limit);
}
