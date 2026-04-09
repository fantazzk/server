package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class SelectDraftPosition {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;

    @Transactional
    public void select(String code, String actionToken, int draftPosition) {
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
        room.selectDraftPosition(caller.getId(), draftPosition);
        rooms.save(room);
    }
}
