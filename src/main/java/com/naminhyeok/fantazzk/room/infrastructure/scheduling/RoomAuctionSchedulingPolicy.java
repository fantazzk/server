package com.naminhyeok.fantazzk.room.infrastructure.scheduling;

import com.naminhyeok.fantazzk.room.application.support.RoomSchedulingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RoomAuctionSchedulingPolicy {
    private final RoomAuctionDeadlineScheduler roomAuctionDeadlineScheduler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(RoomSchedulingEvent event) {
        roomAuctionDeadlineScheduler.refresh(event.roomCode(), event.roundEndsAt());
    }
}
