package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomTeamLeader;
import org.springframework.stereotype.Component;

@Component
public class RoomActionAuthorizer {
    public RoomTeamLeader authenticate(Room room, String actionToken) {
        if (actionToken == null || actionToken.isBlank()) {
            throw CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED);
        }

        return room.getLeaders().stream()
            .filter(leader -> leader.getActionToken().equals(actionToken))
            .findFirst()
            .orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_INVALID));
    }
}
