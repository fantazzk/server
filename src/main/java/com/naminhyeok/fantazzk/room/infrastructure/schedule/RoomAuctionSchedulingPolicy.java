package com.naminhyeok.fantazzk.room.infrastructure.schedule;

import com.naminhyeok.fantazzk.room.domain.RoomSchedulingEvent;
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
