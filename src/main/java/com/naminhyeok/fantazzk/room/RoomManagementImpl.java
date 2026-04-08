package com.naminhyeok.fantazzk.room;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class RoomManagementImpl implements RoomManagement {
    private final CreateRoom createRoom;
    private final GetRoom getRoom;
    private final JoinRoom joinRoom;
    private final StartRoom startRoom;

    @Override
    public RoomView create(UUID templateId, String hostNickname) {
        return RoomView.from(createRoom.create(templateId, hostNickname));
    }

    @Override
    public RoomView get(String code) {
        return RoomView.from(getRoom.get(code));
    }

    @Override
    public RoomView join(String code, String nickname) {
        joinRoom.join(code, nickname);
        return RoomView.from(getRoom.get(code));
    }

    @Override
    public RoomView start(String code) {
        startRoom.start(code);
        return RoomView.from(getRoom.get(code));
    }
}
