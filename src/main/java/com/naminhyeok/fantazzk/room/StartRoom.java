package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.auction.AuctionRoomLifecycle;
import com.naminhyeok.fantazzk.auction.AuctionRoomState;
import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.draft.DraftRoomLifecycle;
import com.naminhyeok.fantazzk.draft.DraftRoomState;
import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class StartRoom {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final RoomSnapshotPublisher roomSnapshotPublisher;
    private final Clock clock;
    private final DraftRoomLifecycle draftRoomLifecycle;
    private final AuctionRoomLifecycle auctionRoomLifecycle;
    private final RoomAuctionDeadlineScheduler roomAuctionDeadlineScheduler;

    @Autowired
    StartRoom(
        Rooms rooms,
        RoomActionAuthorizer roomActionAuthorizer,
        RoomSnapshotPublisher roomSnapshotPublisher,
        Clock clock,
        DraftRoomLifecycle draftRoomLifecycle,
        AuctionRoomLifecycle auctionRoomLifecycle,
        RoomAuctionDeadlineScheduler roomAuctionDeadlineScheduler
    ) {
        this.rooms = rooms;
        this.roomActionAuthorizer = roomActionAuthorizer;
        this.roomSnapshotPublisher = roomSnapshotPublisher;
        this.clock = clock;
        this.draftRoomLifecycle = draftRoomLifecycle;
        this.auctionRoomLifecycle = auctionRoomLifecycle;
        this.roomAuctionDeadlineScheduler = roomAuctionDeadlineScheduler;
    }

    StartRoom(
        Rooms rooms,
        RoomActionAuthorizer roomActionAuthorizer,
        RoomSnapshotPublisher roomSnapshotPublisher,
        Clock clock
    ) {
        this(rooms, roomActionAuthorizer, roomSnapshotPublisher, clock, null, null, null);
    }

    @Transactional
    public Room start(String code, String actionToken) {
        try {
            Room loaded = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(loaded, actionToken);
            Instant now = Instant.now(clock);
            if (loaded.getMode() == RoomMode.DRAFT && draftRoomLifecycle != null) {
                try {
                    DraftRoomState draftState = draftRoomLifecycle.start(code);
                    loaded.start(caller.getId(), now);
                    loaded.applyDraftState(draftState);
                } catch (RuntimeException ex) {
                    loaded.start(caller.getId(), now);
                }
            } else if (loaded.getMode() == RoomMode.AUCTION && auctionRoomLifecycle != null) {
                try {
                    AuctionRoomState auctionState = auctionRoomLifecycle.start(code, caller.getId().value(), now);
                    loaded.start(caller.getId(), now);
                    loaded.applyAuctionState(auctionState);
                } catch (RuntimeException ex) {
                    loaded.start(caller.getId(), now);
                }
            } else {
                loaded.start(caller.getId(), now);
            }
            Room saved = rooms.saveAndFlush(loaded);
            roomSnapshotPublisher.publishAfterCommit(saved);
            if (saved.getMode() == RoomMode.AUCTION && roomAuctionDeadlineScheduler != null) {
                roomAuctionDeadlineScheduler.refreshAfterCommit(saved.getCode(), saved.getCurrentAuctionRoundEndsAt());
            }
            return rooms.findByCode(code).orElse(saved);
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }
}
