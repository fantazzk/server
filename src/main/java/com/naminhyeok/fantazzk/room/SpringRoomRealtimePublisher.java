package com.naminhyeok.fantazzk.room;

import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
class SpringRoomRealtimePublisher implements RoomRealtimePublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Clock clock;

    @Override
    public void publishAfterCommit(Room room) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(room);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(room);
            }
        });
    }

    private void publish(Room room) {
        applicationEventPublisher.publishEvent(RoomRealtimeSnapshotEvent.from(room, Instant.now(clock)));
    }
}
