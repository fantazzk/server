package com.naminhyeok.fantazzk.room;

import org.springframework.stereotype.Service;

public interface RoomStartApi {
    GameView start(String code, String actionToken);
}

@Service
class ProvideRoomStartApi implements RoomStartApi {
    private final StartRoom startRoom;

    ProvideRoomStartApi(StartRoom startRoom) {
        this.startRoom = startRoom;
    }

    @Override
    public GameView start(String code, String actionToken) {
        return GameView.from(startRoom.start(code, actionToken));
    }
}
