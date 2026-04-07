package com.naminhyeok.fantazzk.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PickDraft {
    private final Rooms rooms;

    @Transactional
    public RoomTeamMember pick(String code, String teamLeaderId, String playerName) {
        Room room = rooms.findByCode(code).orElseThrow(() -> RoomException.notFound(code));
        RoomTeamMember member = room.pick(teamLeaderId, playerName);
        rooms.save(room);
        return member;
    }
}
