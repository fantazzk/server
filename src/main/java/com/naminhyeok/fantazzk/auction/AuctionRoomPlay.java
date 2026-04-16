package com.naminhyeok.fantazzk.auction;

import java.time.Instant;
import org.jmolecules.ddd.annotation.Service;

@Service
public class AuctionRoomPlay {
    private final AuctionRooms rooms;

    public AuctionRoomPlay(AuctionRooms rooms) {
        this.rooms = rooms;
    }

    public AuctionBid placeBid(String code, String leaderId, int amount, Instant now) {
        AuctionRoom room = rooms.findByCode(code).orElseThrow(() -> AuctionRoomException.roomNotFound(code));
        AuctionBid bid = room.placeBid(leaderId, amount, now);
        rooms.saveAndFlush(room);
        return bid;
    }
}
