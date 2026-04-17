package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.auction.AuctionRoomPlay;
import com.naminhyeok.fantazzk.auction.AuctionRoomState;
import com.naminhyeok.fantazzk.auction.AuctionRoomStateReader;
import com.naminhyeok.fantazzk.CoreException;
import java.time.Clock;
import java.time.Instant;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@org.jmolecules.ddd.annotation.Service
class PlaceBid {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final RoomSnapshotPublisher roomSnapshotPublisher;
    private final Clock clock;
    private final AuctionRoomPlay auctionRoomPlay;
    private final AuctionRoomStateReader auctionRoomStateReader;
    private final RoomAuctionDeadlineScheduler roomAuctionDeadlineScheduler;

    PlaceBid(
        Rooms rooms,
        RoomActionAuthorizer roomActionAuthorizer,
        RoomSnapshotPublisher roomSnapshotPublisher,
        Clock clock,
        AuctionRoomPlay auctionRoomPlay,
        AuctionRoomStateReader auctionRoomStateReader,
        RoomAuctionDeadlineScheduler roomAuctionDeadlineScheduler
    ) {
        this.rooms = rooms;
        this.roomActionAuthorizer = roomActionAuthorizer;
        this.roomSnapshotPublisher = roomSnapshotPublisher;
        this.clock = clock;
        this.auctionRoomPlay = auctionRoomPlay;
        this.auctionRoomStateReader = auctionRoomStateReader;
        this.roomAuctionDeadlineScheduler = roomAuctionDeadlineScheduler;
    }

    @Transactional
    public Room place(String code, String actionToken, int amount) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            Instant now = Instant.now(clock);
            if (auctionRoomPlay != null && auctionRoomStateReader != null) {
                try {
                    com.naminhyeok.fantazzk.auction.AuctionBid bid = auctionRoomPlay.placeBid(code, caller.getId().value(), amount, now);
                    AuctionRoomState auctionState = auctionRoomStateReader.read(code);
                    room.applyAuctionState(auctionState);
                    room.recordBidProjection(bid.round(), bid.sequence(), caller.getId(), bid.amount());
                    if (roomAuctionDeadlineScheduler != null) {
                        roomAuctionDeadlineScheduler.refreshAfterCommit(code, auctionState.currentRoundEndsAt());
                    }
                } catch (RuntimeException ex) {
                    room.placeBid(caller.getId(), amount, now);
                }
            } else {
                room.placeBid(caller.getId(), amount, now);
            }
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(saved);
            return rooms.findByCode(code).orElse(saved);
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }
}
