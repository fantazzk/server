package com.naminhyeok.fantazzk.room.query;

import com.naminhyeok.fantazzk.room.domain.Room;
import java.util.List;

public interface JoinableRoomReader {
    List<Room> findLatestWaitingRooms(int limit);
}
