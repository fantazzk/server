package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ClearDraftPosition {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;

    @Transactional
    public void clear(String code, String actionToken) {
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
        room.clearDraftPosition(caller.getTeamLeaderId());
        rooms.save(room);
    }
}
