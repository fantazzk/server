package com.naminhyeok.fantazzk.room.event;

import java.time.Instant;
import org.jmolecules.event.types.DomainEvent;

public sealed interface RoomSchedulingEvent extends DomainEvent permits RoomStarted, BidPlaced, AuctionSettled {
    String roomCode();

    Instant roundEndsAt();
}
