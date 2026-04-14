package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import java.time.Clock;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SettleAuctionAttempt {
    private final Rooms rooms;
    private final Games games;
    private final Clock clock;
    private final RoomSnapshotPublisher roomSnapshotPublisher;

    @Autowired
    SettleAuctionAttempt(Rooms rooms, Games games, Clock clock, RoomSnapshotPublisher roomSnapshotPublisher) {
        this.rooms = rooms;
        this.games = games;
        this.clock = clock;
        this.roomSnapshotPublisher = roomSnapshotPublisher;
    }

    SettleAuctionAttempt(Rooms rooms, Clock clock, RoomSnapshotPublisher roomSnapshotPublisher) {
        this(rooms, null, clock, roomSnapshotPublisher);
    }

    @Transactional
    AuctionSettlement settle(String code) {
        try {
            Instant now = Instant.now(clock);
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            AuctionGame game = requireAuctionGame(room);
            AuctionSettlement settlement = game.settleAuction(now);
            games.save(game);
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(new RoomDetails(saved, game));
            return settlement;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    @Transactional
    Room settleIfDue(String code) {
        Instant now = Instant.now(clock);
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        AuctionGame game = loadAuctionGame(room);
        if (!isDue(room, game, now)) {
            return room;
        }

        game.settleAuction(now);
        games.save(game);
        Room saved = rooms.saveAndFlush(room);
        roomSnapshotPublisher.publishAfterCommit(new RoomDetails(saved, game));
        return saved;
    }

    private AuctionGame loadAuctionGame(Room room) {
        if (room.getMode() != RoomMode.AUCTION || room.getStatus() != RoomStatus.STARTED) {
            return null;
        }
        if (games == null || room.getStartedGameId() == null) {
            throw RoomStateInvalidException.auctionRoundMissing();
        }
        Game game = games.findById(room.getStartedGameId()).orElseThrow(RoomStateInvalidException::auctionRoundMissing);
        if (!(game instanceof AuctionGame auctionGame)) {
            throw RoomStateInvalidException.auctionRoundMissing();
        }
        return auctionGame;
    }

    private AuctionGame requireAuctionGame(Room room) {
        if (room.getMode() != RoomMode.AUCTION) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE);
        }
        if (room.getStatus() != RoomStatus.STARTED) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        AuctionGame game = loadAuctionGame(room);
        if (game == null) {
            throw RoomStateInvalidException.auctionRoundMissing();
        }
        return game;
    }

    private static boolean isDue(Room room, AuctionGame game, Instant now) {
        return game != null && game.isDue(now);
    }
}
