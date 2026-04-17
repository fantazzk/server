package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Component
@org.jmolecules.ddd.annotation.Service
@RequiredArgsConstructor
class SettleAuction {
    private final SettleAuctionAttempt settleAuctionAttempt;
    private final Rooms rooms;
    private final ObjectProvider<RoomAuctionDeadlineScheduler> roomAuctionDeadlineScheduler;

    public AuctionSettlement settle(String code) {
        return settleAuctionAttempt.settle(code);
    }

    public Room settleIfDue(String code) {
        try {
            Room room = settleAuctionAttempt.settleIfDue(code);
            scheduleIfNeeded(room);
            return room;
        } catch (OptimisticLockingFailureException ex) {
            return rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        }
    }

    private void scheduleIfNeeded(Room room) {
        if (room.getMode() != RoomMode.AUCTION || room.getStatus() != RoomStatus.IN_PROGRESS) {
            return;
        }
        if (room.getCurrentAuctionRoundEndsAt() == null) {
            return;
        }
        roomAuctionDeadlineScheduler.ifAvailable(scheduler -> scheduler.refreshAfterCommit(room.getCode(), room.getCurrentAuctionRoundEndsAt()));
    }
}
