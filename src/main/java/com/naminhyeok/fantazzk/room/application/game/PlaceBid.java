package com.naminhyeok.fantazzk.room.application.game;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.port.RoomSnapshotPublisher;
import com.naminhyeok.fantazzk.room.application.support.RoomActionAuthorizer;
import com.naminhyeok.fantazzk.room.application.support.StartedGameActionContext;
import com.naminhyeok.fantazzk.room.application.support.StartedGameContextLoader;
import com.naminhyeok.fantazzk.room.application.support.RoomViewProjector;
import com.naminhyeok.fantazzk.room.application.support.StartedRoomSnapshot;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceBid {
    private final Rooms rooms;
    private final Games games;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final StartedGameContextLoader startedGameContextLoader;
    private final RoomSnapshotPublisher roomSnapshotPublisher;
    private final Clock clock;

    @Transactional
    public AuctionBid place(String code, String actionToken, int amount) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            AuctionGame game = requireAuctionGame(room);
            AuctionBid bid = game.placeBid(caller.getId(), amount, Instant.now(clock));
            games.save(game);
            Room saved = rooms.saveAndFlush(room);
            roomSnapshotPublisher.publishAfterCommit(
                new StartedRoomSnapshot(saved.getCode(), saved.getVersion() + game.getVersion(), RoomViewProjector.toGameView(game))
            );
            return bid;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    @Transactional
    public AuctionBid place(UUID gameId, String actionToken, int amount) {
        try {
            StartedGameActionContext action = startedGameContextLoader.authenticate(gameId, actionToken);
            AuctionGame auctionGame = requireAuctionGame(action.game());
            AuctionBid bid = auctionGame.placeBid(action.caller().getId(), amount, Instant.now(clock));
            games.save(auctionGame);
            Room saved = rooms.saveAndFlush(action.room());
            roomSnapshotPublisher.publishAfterCommit(
                new StartedRoomSnapshot(
                    saved.getCode(),
                    saved.getVersion() + auctionGame.getVersion(),
                    RoomViewProjector.toGameView(auctionGame)
                )
            );
            return bid;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    private AuctionGame requireAuctionGame(Room room) {
        if (room.getMode() != RoomMode.AUCTION) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE);
        }
        if (room.getStatus() != RoomStatus.STARTED) {
            throw CoreException.of(RoomErrorType.ROOM_PLAY_REQUIRES_IN_PROGRESS);
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

    private AuctionGame requireAuctionGame(Game game) {
        if (!(game instanceof AuctionGame auctionGame)) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE);
        }
        return auctionGame;
    }
}
