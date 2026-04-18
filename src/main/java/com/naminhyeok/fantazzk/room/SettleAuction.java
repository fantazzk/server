package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import com.naminhyeok.fantazzk.room.application.port.AuctionDeadlineSettlementProcessor;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
class SettleAuction implements AuctionDeadlineSettlementProcessor {
    private final SettleAuctionAttempt settleAuctionAttempt;
    private final Rooms rooms;

    SettleAuction(SettleAuctionAttempt settleAuctionAttempt, Rooms rooms) {
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

    @Override
    public void processDueAuction(String code) {
        settleIfDue(code);
    }

    public Game settleIfDue(UUID gameId) {
        return settleAuctionAttempt.settleIfDue(gameId);
    }
}
