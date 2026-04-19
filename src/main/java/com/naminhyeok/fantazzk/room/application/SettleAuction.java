package com.naminhyeok.fantazzk.room.application;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.domain.AuctionSettlement;
import com.naminhyeok.fantazzk.room.domain.Game;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomErrorType;
import com.naminhyeok.fantazzk.room.repository.Rooms;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
public class SettleAuction {
    private final SettleAuctionAttempt settleAuctionAttempt;
    private final Rooms rooms;

    public SettleAuction(SettleAuctionAttempt settleAuctionAttempt, Rooms rooms) {
        this.settleAuctionAttempt = settleAuctionAttempt;
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

    public Game settleIfDue(UUID gameId) {
        return settleAuctionAttempt.settleIfDue(gameId);
    }
}
