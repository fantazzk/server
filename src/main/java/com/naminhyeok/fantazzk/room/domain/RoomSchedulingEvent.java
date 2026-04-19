package com.naminhyeok.fantazzk.room.domain;

import java.time.Instant;
import org.jmolecules.event.types.DomainEvent;

public sealed interface RoomSchedulingEvent extends DomainEvent permits RoomStarted, BidPlaced, AuctionSettled {
    public String roomCode();

    public Instant roundEndsAt();
}
