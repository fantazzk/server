package com.naminhyeok.fantazzk.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JoinRoom {
    private final Rooms rooms;

    @Transactional
    public RoomTeamLeader join(String code, String nickname) {
        Room room = rooms.findByCode(code).orElseThrow(() -> RoomException.notFound(code));
        room.join(java.util.UUID.randomUUID().toString(), nickname);
        Room saved = rooms.save(room);
        return saved.getLeaders().getLast();
    }
}
