package com.naminhyeok.fantazzk.room;

import java.util.List;

interface JoinableRoomReader {
    List<Room> findLatestWaitingRooms(int limit);
}
