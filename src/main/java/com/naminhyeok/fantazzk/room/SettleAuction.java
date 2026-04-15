package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
class SettleAuction {
    private final SettleAuctionAttempt settleAuctionAttempt;
    private final Rooms rooms;

    SettleAuction(SettleAuctionAttempt settleAuctionAttempt, Rooms rooms) {
        this.settleAuctionAttempt = settleAuctionAttempt;
        this.rooms = rooms;
    }

    public AuctionSettlement settle(String code) {
        return settleAuctionAttempt.settle(code);
    }

    public Room settleIfDue(String code) {
        try {
            return settleAuctionAttempt.settleIfDue(code);
        } catch (OptimisticLockingFailureException ex) {
            return rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        }
    }
}
