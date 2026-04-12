package com.naminhyeok.fantazzk.room;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class RoomAuctionDeadlineScheduler {
    private static final int CATCH_UP_BATCH_SIZE = 200;
    private static final PageRequest CATCH_UP_PAGE = PageRequest.of(0, 200);

    private final TaskScheduler taskScheduler;
    private final SettleAuction settleAuction;
    private final Rooms rooms;
    private final Clock clock;
    private final ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    void schedule(Room room) {
        cancel(room.getCode());
        if (!isSchedulable(room)) {
            return;
        }

        String code = room.getCode();
        ScheduledFuture<?> future =
            taskScheduler.schedule(() -> runScheduledSettlement(code), room.getCurrentAuctionRoundEndsAt());
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
        for (Room room : collectSchedulableRooms()) {
            if (room.getCurrentAuctionRoundEndsAt() == null || !room.getCurrentAuctionRoundEndsAt().isAfter(now)) {
                try {
                    schedule(settleAuction.settleIfDue(room.getCode()));
                } catch (RoomStateInvalidException ignored) {
                    continue;
                }
                continue;
            }
            schedule(room);
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

    private List<Room> collectSchedulableRooms() {
        List<Room> schedulableRooms = new ArrayList<>();
        int page = 0;
        while (true) {
            List<Room> batch =
                rooms.findByStatusAndModeOrderByCodeAsc(
                    RoomStatus.IN_PROGRESS,
                    RoomMode.AUCTION,
                    PageRequest.of(page, CATCH_UP_BATCH_SIZE)
                );
            if (batch.isEmpty()) {
                return sortSchedulableRooms(schedulableRooms);
            }
            schedulableRooms.addAll(batch);
            if (batch.size() < CATCH_UP_BATCH_SIZE) {
                return sortSchedulableRooms(schedulableRooms);
            }
            page += 1;
        }
    }

    private List<Room> sortSchedulableRooms(List<Room> rooms) {
        return rooms.stream()
            .sorted(
                Comparator.comparing(
                    Room::getCurrentAuctionRoundEndsAt,
                    Comparator.nullsFirst(Comparator.naturalOrder())
                ).thenComparing(Room::getCode)
            )
            .toList();
    }
}
