package com.naminhyeok.fantazzk.room;

import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

@Getter
class RoomBid implements Entity<Room, RoomBid.RoomBidId> {
    private final RoomBidId id;
    private final int round;
    private final String teamLeaderId;
    private final int amount;

    RoomBid(int round, String teamLeaderId, int amount) {
        this.id = new RoomBidId(UUID.randomUUID());
        this.round = round;
        this.teamLeaderId = teamLeaderId;
        this.amount = amount;
    }

    @Override
    public RoomBidId getId() {
        return id;
    }

    record RoomBidId(UUID roomBidId) implements Identifier {
    }
}
