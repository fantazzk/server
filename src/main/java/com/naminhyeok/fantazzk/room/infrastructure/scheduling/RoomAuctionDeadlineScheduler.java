package com.naminhyeok.fantazzk.room.infrastructure.scheduling;

import com.naminhyeok.fantazzk.InvalidDomainStateException;
import com.naminhyeok.fantazzk.room.application.port.AuctionDeadlineSettlementProcessor;
import com.naminhyeok.fantazzk.room.application.query.AuctionScheduleCandidate;
import com.naminhyeok.fantazzk.room.application.query.AuctionScheduleReader;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoomAuctionDeadlineScheduler {
    private final TaskScheduler taskScheduler;
    private final AuctionDeadlineSettlementProcessor auctionDeadlineSettlementProcessor;
    private final AuctionScheduleReader auctionScheduleReader;
    private final Clock clock;
    private final ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public void schedule(String code, Instant deadline) {
        refresh(code, deadline);
    }

    public void refresh(String code, Instant deadline) {
        cancel(code);
        if (deadline == null) {
            return;
        }
        scheduleTask(code, deadline);
    }

    private void scheduleTask(String code, Instant deadline) {
        ScheduledFuture<?> future = taskScheduler.schedule(() -> runScheduledSettlement(code), deadline);
        if (future != null) {
            scheduledTasks.put(code, future);
        }
    }

    public void cancel(String code) {
        ScheduledFuture<?> future = scheduledTasks.remove(code);
        if (future != null) {
            future.cancel(false);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void catchUpAndReschedule() {
        Instant now = Instant.now(clock);
        for (AuctionScheduleCandidate candidate : collectSchedulableRooms()) {
            if (candidate.deadline() == null || !candidate.deadline().isAfter(now)) {
                try {
                    refresh(candidate.code(), auctionDeadlineSettlementProcessor.processDueAuction(candidate.code()));
                } catch (InvalidDomainStateException ignored) {
                    continue;
                }
                continue;
            }
            scheduleTask(candidate.code(), candidate.deadline());
        }
    }

    private void runScheduledSettlement(String code) {
        scheduledTasks.remove(code);
        refresh(code, auctionDeadlineSettlementProcessor.processDueAuction(code));
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
