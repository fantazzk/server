package com.naminhyeok.fantazzk.room;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RoomAuctionDeadlineScheduler {
    private final TaskScheduler taskScheduler;
    private final SettleAuction settleAuction;
    private final AuctionScheduleReader auctionScheduleReader;
    private final Clock clock;
    private final ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    void schedule(Room room) {
        cancel(room.getCode());
        if (!isSchedulable(room)) {
            return;
        }

        schedule(room.getCode(), room.getCurrentAuctionRoundEndsAt());
    }

    private void schedule(String code, Instant deadline) {
        ScheduledFuture<?> future = taskScheduler.schedule(() -> runScheduledSettlement(code), deadline);
        if (future != null) {
            scheduledTasks.put(code, future);
        }
    }

    void cancel(String code) {
        ScheduledFuture<?> future = scheduledTasks.remove(code);
        if (future != null) {
            future.cancel(false);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    void catchUpAndReschedule() {
        Instant now = Instant.now(clock);
        for (AuctionScheduleCandidate candidate : collectSchedulableRooms()) {
            if (candidate.deadline() == null || !candidate.deadline().isAfter(now)) {
                try {
                    schedule(settleAuction.settleIfDue(candidate.code()));
                } catch (RoomStateInvalidException ignored) {
                    continue;
                }
                continue;
            }
            schedule(candidate.code(), candidate.deadline());
        }
    }

    private void runScheduledSettlement(String code) {
        scheduledTasks.remove(code);
        schedule(settleAuction.settleIfDue(code));
    }

    private static boolean isSchedulable(Room room) {
        return room.getMode() == RoomMode.AUCTION
            && room.getStatus() == RoomStatus.IN_PROGRESS
            && room.getCurrentAuctionRoundEndsAt() != null;
    }

    private List<AuctionScheduleCandidate> collectSchedulableRooms() {
        return sortSchedulableRooms(auctionScheduleReader.findInProgressAuctionSchedules());
    }

    private List<AuctionScheduleCandidate> sortSchedulableRooms(List<AuctionScheduleCandidate> candidates) {
        return candidates.stream()
            .sorted(
                Comparator.comparing(
                    AuctionScheduleCandidate::deadline,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ).thenComparing(AuctionScheduleCandidate::code)
            )
            .toList();
    }
}
