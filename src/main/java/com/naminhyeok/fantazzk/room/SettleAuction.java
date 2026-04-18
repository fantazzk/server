package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.port.AuctionDeadlineSettlementProcessor;
import java.time.Instant;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
class SettleAuction implements AuctionDeadlineSettlementProcessor {
    private final SettleAuctionAttempt settleAuctionAttempt;
    private final Games games;
    private final Rooms rooms;

    SettleAuction(SettleAuctionAttempt settleAuctionAttempt, Games games, Rooms rooms) {
        this.settleAuctionAttempt = settleAuctionAttempt;
        this.games = games;
        this.rooms = rooms;
    }

    public AuctionSettlement settle(String code) {
        return settleAuctionAttempt.settle(code);
    }

    public AuctionSettlement settle(UUID gameId) {
        return settleAuctionAttempt.settle(gameId);
    }

    public Room settleIfDue(String code) {
        try {
            return settleAuctionAttempt.settleIfDue(code);
        } catch (OptimisticLockingFailureException ex) {
            return rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        }
    }

    @Override
    public Instant processDueAuction(String code) {
        Room room = settleIfDue(code);
        return resolveCurrentAuctionDeadline(room);
    }

    public Game settleIfDue(UUID gameId) {
        return settleAuctionAttempt.settleIfDue(gameId);
    }

    private Instant resolveCurrentAuctionDeadline(Room room) {
        if (room.getMode() != RoomMode.AUCTION || room.getStatus() != RoomStatus.STARTED || room.getStartedGameId() == null) {
            return null;
        }
        return games.findById(room.getStartedGameId())
            .filter(AuctionGame.class::isInstance)
            .map(AuctionGame.class::cast)
            .map(AuctionGame::getCurrentRoundEndsAt)
            .orElse(null);
    }
}
