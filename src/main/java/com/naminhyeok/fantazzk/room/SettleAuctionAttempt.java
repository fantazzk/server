package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class SettleAuctionAttempt {
    private final Rooms rooms;
    private final Clock clock;
    private final RoomRealtimePublisher roomRealtimePublisher;

    @Transactional
    AuctionSettlement settle(String code) {
        Instant now = Instant.now(clock);
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        AuctionSettlement settlement = room.settleAuction(now);
        Room saved = rooms.saveAndFlush(room);
        roomRealtimePublisher.publishAfterCommit(saved);
        return settlement;
    }

    @Transactional
    Room settleIfDue(String code) {
        Instant now = Instant.now(clock);
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        if (!isDue(room, now)) {
            return room;
        }

        room.settleAuction(now);
        Room saved = rooms.saveAndFlush(room);
        roomRealtimePublisher.publishAfterCommit(saved);
        return saved;
    }

    private static boolean isDue(Room room, Instant now) {
        return room.getMode() == RoomMode.AUCTION
            && room.getStatus() == RoomStatus.IN_PROGRESS
            && room.getCurrentAuctionRoundEndsAt() != null
            && !room.getCurrentAuctionRoundEndsAt().isAfter(now);
    }
}
