package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
class PlaceBid {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final RoomAuctionDeadlineScheduler roomAuctionDeadlineScheduler;
    private final Clock clock;

    @Transactional
    public RoomBid place(String code, String actionToken, int amount) {
        try {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            RoomBid bid = room.placeBid(caller.getId(), amount, Instant.now(clock));
            Room saved = rooms.saveAndFlush(room);
            scheduleAfterCommit(saved);
            return bid;
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    private void scheduleAfterCommit(Room room) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            roomAuctionDeadlineScheduler.schedule(room);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                roomAuctionDeadlineScheduler.schedule(room);
            }
        });
    }
}
