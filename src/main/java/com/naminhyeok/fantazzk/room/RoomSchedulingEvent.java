package com.naminhyeok.fantazzk.room;

import java.time.Instant;
import org.jmolecules.event.types.DomainEvent;

sealed interface RoomSchedulingEvent extends DomainEvent permits RoomStarted, BidPlaced, AuctionSettled {
    String roomCode();

    Instant roundEndsAt();
}
