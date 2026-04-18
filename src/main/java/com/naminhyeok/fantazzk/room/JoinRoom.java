package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.application.port.TeamLeaderIdentityIssuer;
import com.naminhyeok.fantazzk.room.application.support.RoomSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class JoinRoom {
    private final Rooms rooms;
    private final TeamLeaderIdentityIssuer teamLeaderIdentityIssuer;
    private final RoomSnapshotPublisher roomSnapshotPublisher;

    @Transactional
    public RoomSessionResult join(String code, String nickname) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            TeamLeaderIdentityIssuer.TeamLeaderIdentity identity = teamLeaderIdentityIssuer.issue();
            TeamLeaderId joinedLeaderId = new TeamLeaderId(identity.leaderId());
            room.join(joinedLeaderId, nickname, identity.actionToken());
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(new RoomSnapshot(saved.getCode(), saved.getVersion(), RoomView.from(saved)));
            return new RoomSessionResult(saved, findLeader(saved, joinedLeaderId));
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    private RoomTeamLeader findLeader(Room room, TeamLeaderId leaderId) {
        return room.getLeaders().stream()
            .filter(leader -> leader.getId().equals(leaderId))
            .findFirst()
            .orElseThrow();
    }
}
