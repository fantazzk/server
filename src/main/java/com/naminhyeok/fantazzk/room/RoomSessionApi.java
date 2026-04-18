package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.room.application.room.CreateRoom;
import com.naminhyeok.fantazzk.room.application.room.JoinRoom;
import java.util.UUID;
import org.springframework.stereotype.Service;

public interface RoomSessionApi {
    RoomSessionView create(UUID templateId, String hostNickname);

    RoomSessionView join(String code, String nickname);
}

@Service
class ProvideRoomSessionApi implements RoomSessionApi {
    private final CreateRoom createRoom;
    private final JoinRoom joinRoom;

    ProvideRoomSessionApi(CreateRoom createRoom, JoinRoom joinRoom) {
        this.createRoom = createRoom;
        this.joinRoom = joinRoom;
    }

    @Override
    public RoomSessionView create(UUID templateId, String hostNickname) {
        return RoomSessionView.from(createRoom.create(templateId, hostNickname));
    }

    @Override
    public RoomSessionView join(String code, String nickname) {
        return RoomSessionView.from(joinRoom.join(code, nickname));
    }
}
