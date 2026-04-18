package com.naminhyeok.fantazzk.room;

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
