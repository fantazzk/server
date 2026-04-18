package com.naminhyeok.fantazzk.room.application.query;

import com.naminhyeok.fantazzk.room.JoinableRoomView;
import java.util.List;

public interface JoinableRoomReader {
    List<JoinableRoomView> findLatestWaitingRooms(int limit);
}
