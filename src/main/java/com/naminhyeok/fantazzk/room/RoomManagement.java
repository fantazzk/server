package com.naminhyeok.fantazzk.room;

import java.util.UUID;

public interface RoomManagement {
    RoomView create(UUID templateId, String hostNickname);

    RoomView get(String code);

    RoomView join(String code, String nickname);

    RoomView start(String code);
}
