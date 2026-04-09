package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import org.springframework.stereotype.Component;

@Component
class RoomActionAuthorizer {
    RoomTeamLeader authenticate(Room room, String actionToken) {
        if (actionToken == null || actionToken.isBlank()) {
            throw CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_REQUIRED);
        }

        return room.getLeaders().stream()
            .filter(leader -> leader.getActionToken().equals(actionToken))
            .findFirst()
            .orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_ACTION_TOKEN_INVALID));
    }
}
