package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.event.RoomSchedulingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
class RoomAuctionSchedulingPolicy {
    private final RoomAuctionDeadlineScheduler roomAuctionDeadlineScheduler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void on(RoomSchedulingEvent event) {
        roomAuctionDeadlineScheduler.refresh(event.roomCode(), event.roundEndsAt());
    }
}
