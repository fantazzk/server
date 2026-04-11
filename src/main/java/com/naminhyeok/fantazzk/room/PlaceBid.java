package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
class PlaceBid {
    private final Rooms rooms;
    private final RoomActionAuthorizer roomActionAuthorizer;
    private final RoomAuctionDeadlineScheduler roomAuctionDeadlineScheduler;
    private final Clock clock;
    private final PlatformTransactionManager transactionManager;

    public RoomBid place(String code, String actionToken, int amount) {
        PlaceBidResult result = inTransaction(() -> {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            RoomTeamLeader caller = roomActionAuthorizer.authenticate(room, actionToken);
            RoomBid bid = room.placeBid(caller.getId(), amount, Instant.now(clock));
            Room saved = rooms.save(room);
            scheduleAfterCommit(saved);
            return new PlaceBidResult(saved, bid);
        });
        return result.bid();
    }

    private <T> T inTransaction(TransactionCallback<T> callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        try {
            return Objects.requireNonNull(transactionTemplate.execute(status -> callback.execute()));
        } catch (OptimisticLockingFailureException ex) {
            throw CoreException.of(RoomErrorType.ROOM_CONCURRENT_MODIFICATION);
        }
    }

    private record PlaceBidResult(Room room, RoomBid bid) {}

    @FunctionalInterface
    private interface TransactionCallback<T> {
        T execute();
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
