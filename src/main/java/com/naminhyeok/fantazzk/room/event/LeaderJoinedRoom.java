package com.naminhyeok.fantazzk.room.event;

import org.jmolecules.event.types.DomainEvent;

public record LeaderJoinedRoom(
    String roomCode,
    String leaderId
) implements DomainEvent {
}
