package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class StartRoom {
    private final Rooms rooms;

    @Transactional
    public void start(String code, String actionToken) {
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        RoomTeamLeader caller = authenticate(room, actionToken);
        room.start(caller.getTeamLeaderId());
        rooms.save(room);
    }

    private RoomTeamLeader authenticate(Room room, String actionToken) {
        if (actionToken == null || actionToken.isBlank()) {
            throw CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED);
        }

        return room.getLeaders().stream()
            .filter(leader -> leader.getActionToken().equals(actionToken))
            .findFirst()
            .orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_INVALID));
    }
}
