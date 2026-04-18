package com.naminhyeok.fantazzk.room.application.support;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettleAuctionAttempt {
    private final Rooms rooms;
    private final Games games;
    private final StartedGameContextLoader startedGameContextLoader;
    private final Clock clock;
    private final RoomSnapshotPublisher roomSnapshotPublisher;

    @Transactional
    public AuctionSettlement settle(String code) {
        try {
            Instant now = Instant.now(clock);
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            AuctionGame game = requireAuctionGame(room);
            AuctionSettlement settlement = game.settleAuction(now);
            games.save(game);
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(
                new StartedRoomSnapshot(saved.getCode(), saved.getVersion() + game.getVersion(), RoomViewProjector.toGameView(game))
            );
            return settlement;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    @Transactional
    public AuctionSettlement settle(UUID gameId) {
        try {
            Instant now = Instant.now(clock);
            StartedGameContext context = startedGameContextLoader.load(gameId);
            AuctionGame game = requireAuctionGame(context.game());
            AuctionSettlement settlement = game.settleAuction(now);
            games.save(game);
            Room saved = rooms.saveAndFlush(context.room());
            roomSnapshotPublisher.publishAfterCommit(
                new StartedRoomSnapshot(saved.getCode(), saved.getVersion() + game.getVersion(), RoomViewProjector.toGameView(game))
            );
            return settlement;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    @Transactional
    public Room settleIfDue(String code) {
        Instant now = Instant.now(clock);
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        AuctionGame game = loadAuctionGame(room);
        if (!isDue(room, game, now)) {
            return room;
        }

        game.settleAuction(now);
        games.save(game);
        Room saved = rooms.saveAndFlush(room);
        roomSnapshotPublisher.publishAfterCommit(
            new StartedRoomSnapshot(saved.getCode(), saved.getVersion() + game.getVersion(), RoomViewProjector.toGameView(game))
        );
        return saved;
    }

    @Transactional
    public Game settleIfDue(UUID gameId) {
        Instant now = Instant.now(clock);
        StartedGameContext context = startedGameContextLoader.load(gameId);
        Game loadedGame = context.game();
        AuctionGame game = loadAuctionGame(loadedGame);
        if (!isDue(game, now)) {
            return loadedGame;
        }

        game.settleAuction(now);
        games.save(game);
        Room saved = rooms.saveAndFlush(context.room());
        roomSnapshotPublisher.publishAfterCommit(
            new StartedRoomSnapshot(saved.getCode(), saved.getVersion() + game.getVersion(), RoomViewProjector.toGameView(game))
        );
        return game;
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

    private AuctionGame loadAuctionGame(Game game) {
        if (!(game instanceof AuctionGame auctionGame)) {
            return null;
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

    private AuctionGame requireAuctionGame(Game game) {
        AuctionGame auctionGame = loadAuctionGame(game);
        if (auctionGame == null) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE);
        }
        if (auctionGame.getStatus() != GameStatus.IN_PROGRESS) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
        }
        return auctionGame;
    }

    private static boolean isDue(AuctionGame game, Instant now) {
        return game != null && game.isDue(now);
    }

    private static boolean isDue(Room room, AuctionGame game, Instant now) {
        return isDue(game, now);
    }
}
