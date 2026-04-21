package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.AuctionBid;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.domain.StartedRoomSnapshot;
import com.naminhyeok.fantazzk.room.repository.Games;
import com.naminhyeok.fantazzk.room.repository.Rooms;
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
    private final StartedGameContextLoader startedGameContextLoader;
    private final RoomRealtimeEventPublisher realtimeEventPublisher;
    private final Clock clock;

    @Transactional
    public AuctionBid place(UUID gameId, String actionToken, int amount) {
        try {
            StartedGameActionContext action = startedGameContextLoader.authenticate(gameId, actionToken);
            AuctionGame auctionGame = requireAuctionGame(action.game());
            AuctionBid bid = auctionGame.placeBid(action.caller().getId(), amount, Instant.now(clock));
            games.save(auctionGame);
            Room saved = rooms.saveAndFlush(action.room());
            realtimeEventPublisher.publishGameUpdatedAfterCommit(new StartedRoomSnapshot(saved, auctionGame));
            return bid;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    private AuctionGame requireAuctionGame(Game game) {
        if (!(game instanceof AuctionGame auctionGame)) {
            throw CoreException.of(RoomErrorType.ROOM_BID_REQUIRES_AUCTION_MODE);
        }
        return auctionGame;
    }
}
