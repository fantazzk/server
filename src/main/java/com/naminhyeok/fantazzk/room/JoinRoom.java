package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.auction.AuctionRoomLifecycle;
import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.draft.DraftRoomLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class JoinRoom {
    private final Rooms rooms;
    private final TeamLeaderIdentityIssuer teamLeaderIdentityIssuer;
    private final RoomSnapshotPublisher roomSnapshotPublisher;
    private final DraftRoomLifecycle draftRoomLifecycle;
    private final AuctionRoomLifecycle auctionRoomLifecycle;

    @Autowired
    JoinRoom(
        Rooms rooms,
        TeamLeaderIdentityIssuer teamLeaderIdentityIssuer,
        RoomSnapshotPublisher roomSnapshotPublisher,
        DraftRoomLifecycle draftRoomLifecycle,
        AuctionRoomLifecycle auctionRoomLifecycle
    ) {
        this.rooms = rooms;
        this.teamLeaderIdentityIssuer = teamLeaderIdentityIssuer;
        this.roomSnapshotPublisher = roomSnapshotPublisher;
        this.draftRoomLifecycle = draftRoomLifecycle;
        this.auctionRoomLifecycle = auctionRoomLifecycle;
    }

    JoinRoom(Rooms rooms, TeamLeaderIdentityIssuer teamLeaderIdentityIssuer, RoomSnapshotPublisher roomSnapshotPublisher) {
        this(rooms, teamLeaderIdentityIssuer, roomSnapshotPublisher, null, null);
    }

    @Transactional
    public RoomSessionResult join(String code, String nickname) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            TeamLeaderIdentityIssuer.TeamLeaderIdentity identity = teamLeaderIdentityIssuer.issue();
            TeamLeaderId leaderId = new TeamLeaderId(identity.leaderId());
            room.join(leaderId, nickname, identity.actionToken());
            if (room.getMode() == RoomMode.DRAFT && draftRoomLifecycle != null) {
                draftRoomLifecycle.addLeader(code, leaderId.value(), nickname);
            } else if (room.getMode() == RoomMode.AUCTION && auctionRoomLifecycle != null) {
                auctionRoomLifecycle.addLeader(code, leaderId.value(), nickname);
            }
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(saved);
            return new RoomSessionResult(saved, saved.getLeaders().getLast());
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }
}
