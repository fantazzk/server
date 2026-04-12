package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class JoinRoom {
    private final Rooms rooms;
    private final TeamLeaderIdentityIssuer teamLeaderIdentityIssuer;
    private final RoomRealtimePublisher roomRealtimePublisher;

    @Transactional
    public RoomTeamLeader join(String code, String nickname) {
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        TeamLeaderIdentityIssuer.TeamLeaderIdentity identity = teamLeaderIdentityIssuer.issue();
        room.join(new TeamLeaderId(identity.leaderId()), nickname, identity.actionToken());
        Room saved = rooms.save(room);
        roomRealtimePublisher.publishAfterCommit(saved);
        return saved.getLeaders().getLast();
    }
}
