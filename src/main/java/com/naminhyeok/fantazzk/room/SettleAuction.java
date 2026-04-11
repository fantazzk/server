package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
class SettleAuction {
    private final Rooms rooms;
    private final Clock clock;
    private final PlatformTransactionManager transactionManager;
    private final ObjectProvider<RoomAuctionDeadlineScheduler> roomAuctionDeadlineScheduler;

    public AuctionSettlement settle(String code) {
        Instant now = Instant.now(clock);
        return inTransaction(() -> {
            Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
            AuctionSettlement settlement = room.settleAuction(now);
            rooms.save(room);
            return settlement;
        });
    }

    public Room settleIfDue(String code) {
        Instant now = Instant.now(clock);
        try {
            Room room = inTransaction(() -> settleIfDueInTransaction(code, now));
            scheduleIfNeeded(room);
            return room;
        } catch (org.springframework.dao.OptimisticLockingFailureException ex) {
            return rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        }
    }

    private Room settleIfDueInTransaction(String code, Instant now) {
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        if (room.repairMissingAuctionDeadline(now)) {
            return rooms.saveAndFlush(room);
        }
        if (!isDue(room, now)) {
            return room;
        }

        room.settleAuction(now);
        return rooms.saveAndFlush(room);
    }

    private void scheduleIfNeeded(Room room) {
        if (room.getMode() != RoomMode.AUCTION || room.getStatus() != RoomStatus.IN_PROGRESS) {
            return;
        }
        if (room.getCurrentAuctionRoundEndsAt() == null) {
            return;
        }
        roomAuctionDeadlineScheduler.ifAvailable(scheduler -> scheduler.schedule(room));
    }

    private static boolean isDue(Room room, Instant now) {
        return room.getMode() == RoomMode.AUCTION
            && room.getStatus() == RoomStatus.IN_PROGRESS
            && room.getCurrentAuctionRoundEndsAt() != null
            && !room.getCurrentAuctionRoundEndsAt().isAfter(now);
    }

    private <T> T inTransaction(TransactionCallback<T> callback) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return Objects.requireNonNull(transactionTemplate.execute(status -> callback.execute()));
    }

    @FunctionalInterface
    private interface TransactionCallback<T> {
        T execute();
    }
}
