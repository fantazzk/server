package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.auction.AuctionRoomLifecycle;
import com.naminhyeok.fantazzk.auction.AuctionRoomState;
import com.naminhyeok.fantazzk.auction.AuctionRoomStateReader;
import com.naminhyeok.fantazzk.CoreException;
import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SettleAuctionAttempt {
    private final Rooms rooms;
    private final Clock clock;
    private final RoomSnapshotPublisher roomSnapshotPublisher;
    private final AuctionRoomLifecycle auctionRoomLifecycle;
    private final AuctionRoomStateReader auctionRoomStateReader;

    @Autowired
    SettleAuctionAttempt(
        Rooms rooms,
        Clock clock,
        RoomSnapshotPublisher roomSnapshotPublisher,
        AuctionRoomLifecycle auctionRoomLifecycle,
        AuctionRoomStateReader auctionRoomStateReader
    ) {
        this.rooms = rooms;
        this.clock = clock;
        this.roomSnapshotPublisher = roomSnapshotPublisher;
        this.auctionRoomLifecycle = auctionRoomLifecycle;
        this.auctionRoomStateReader = auctionRoomStateReader;
    }

    SettleAuctionAttempt(Rooms rooms, Clock clock, RoomSnapshotPublisher roomSnapshotPublisher) {
        this(rooms, clock, roomSnapshotPublisher, null, null);
    }

    @Transactional
    AuctionSettlement settle(String code) {
        Instant now = Instant.now(clock);
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        if (auctionRoomLifecycle == null || auctionRoomStateReader == null) {
            AuctionSettlement settlement = room.settleAuction(now);
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(saved);
            return settlement;
        }
        com.naminhyeok.fantazzk.auction.AuctionSettlement settlement;
        try {
            settlement = auctionRoomLifecycle.settle(code, now);
            AuctionRoomState auctionState = auctionRoomStateReader.read(code);
            room.applyAuctionState(auctionState);
        } catch (RuntimeException ex) {
            AuctionSettlement legacySettlement = room.settleAuction(now);
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(saved);
            return legacySettlement;
        }
        Room saved = rooms.saveAndFlush(room);
        roomSnapshotPublisher.publishAfterCommit(saved);
        return new AuctionSettlement(
            new RoomPlayerId(settlement.playerId()),
            settlement.playerName(),
            AuctionOutcome.valueOf(settlement.outcome().name())
        );
    }

    @Transactional
    Room settleIfDue(String code) {
        Instant now = Instant.now(clock);
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        if (!isDue(room, now)) {
            return room;
        }

        if (auctionRoomLifecycle == null || auctionRoomStateReader == null) {
            room.settleAuction(now);
        } else {
            try {
                auctionRoomLifecycle.settle(code, now);
                AuctionRoomState auctionState = auctionRoomStateReader.read(code);
                room.applyAuctionState(auctionState);
            } catch (RuntimeException ex) {
                room.settleAuction(now);
            }
        }
        Room saved = rooms.saveAndFlush(room);
        roomSnapshotPublisher.publishAfterCommit(saved);
        return saved;
    }

    private static boolean isDue(Room room, Instant now) {
        return room.getMode() == RoomMode.AUCTION
            && room.getStatus() == RoomStatus.IN_PROGRESS
            && room.getCurrentAuctionRoundEndsAt() != null
            && !room.getCurrentAuctionRoundEndsAt().isAfter(now);
    }
}
