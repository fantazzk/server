package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.room.application.query.FindJoinableRooms;
import com.naminhyeok.fantazzk.room.application.room.GetRoom;
import java.util.List;
import org.springframework.stereotype.Service;

public interface RoomQueryApi {
    List<JoinableRoomView> list();

    RoomView get(String code);
}

@Service
class ProvideRoomQueryApi implements RoomQueryApi {
    private final GetRoom getRoom;
    private final FindJoinableRooms findJoinableRooms;

    ProvideRoomQueryApi(GetRoom getRoom, FindJoinableRooms findJoinableRooms) {
        this.getRoom = getRoom;
        this.findJoinableRooms = findJoinableRooms;
    }

    @Override
    public List<JoinableRoomView> list() {
        return findJoinableRooms.list();
    }

    @Override
    public RoomView get(String code) {
        return RoomView.from(getRoom.get(code));
    }
}
