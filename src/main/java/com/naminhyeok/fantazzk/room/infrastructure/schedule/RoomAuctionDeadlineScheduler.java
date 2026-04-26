package com.naminhyeok.fantazzk.room.infrastructure.schedule;

import com.naminhyeok.fantazzk.room.application.AuctionSettlementRunner;
import com.naminhyeok.fantazzk.room.domain.AuctionGame;
import com.naminhyeok.fantazzk.room.domain.Room;
import com.naminhyeok.fantazzk.room.domain.RoomMode;
import com.naminhyeok.fantazzk.room.domain.RoomStateInvalidException;
import com.naminhyeok.fantazzk.room.domain.RoomStatus;
import com.naminhyeok.fantazzk.room.query.AuctionScheduleCandidate;
import com.naminhyeok.fantazzk.room.query.AuctionScheduleReader;
import com.naminhyeok.fantazzk.room.repository.Games;
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
public class RoomAuctionDeadlineScheduler {
    private final TaskScheduler taskScheduler;
    private final AuctionSettlementRunner settleAuction;
    private final AuctionScheduleReader auctionScheduleReader;
    private final Clock clock;
    private final Games games;
    private final ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    void schedule(Room room) {
        refresh(room.getCode(), resolveDeadline(room));
    }

    void refresh(String code, Instant deadline) {
        cancel(code);
        if (deadline == null) {
            return;
        }
        schedule(code, deadline);
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
    public void catchUpAndReschedule() {
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

    private Instant resolveDeadline(Room room) {
        if (room.getMode() != RoomMode.AUCTION || room.getStatus() != RoomStatus.STARTED) {
            return null;
        }
        if (room.getStartedGameId() == null) {
            return null;
        }
        return games.findById(room.getStartedGameId())
            .filter(AuctionGame.class::isInstance)
            .map(AuctionGame.class::cast)
            .map(AuctionGame::getCurrentRoundEndsAt)
            .orElse(null);
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
