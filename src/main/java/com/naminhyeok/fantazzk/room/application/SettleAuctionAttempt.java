package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomStateInvalidException;
import com.naminhyeok.fantazzk.room.domain.RoomStatus;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettleAuctionAttempt {
    private final Rooms rooms;
    private final Games games;
    private final Clock clock;
    private final RoomRealtimeEventPublisher realtimeEventPublisher;

    @Transactional
    public Room settleIfDue(String code) {
        Instant now = Instant.now(clock);
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        AuctionGame game = loadAuctionGame(room);
        if (!isDue(game, now)) {
            return room;
        }

        game.settleAuction(now);
        games.save(game);
        Room saved = rooms.saveAndFlush(room);
        publishAuctionSettlementEvents(saved, game);
        return saved;
    }

    private void publishAuctionSettlementEvents(Room room, AuctionGame game) {
        StartedRoomSnapshot snapshot = new StartedRoomSnapshot(room, game);
        realtimeEventPublisher.publishGameUpdatedAfterCommit(snapshot);
    }

    private AuctionGame loadAuctionGame(Room room) {
        if (room.getMode() != RoomMode.AUCTION || room.getStatus() != RoomStatus.STARTED) {
            return null;
        }
        if (room.getStartedGameId() == null) {
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

    private static boolean isDue(AuctionGame game, Instant now) {
        return game != null && game.isDue(now);
    }
}
