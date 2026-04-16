package com.naminhyeok.fantazzk.auction;

import org.jmolecules.ddd.annotation.Service;

@Service
public class AuctionRoomStateReader {
    private final AuctionRooms rooms;

    public AuctionRoomStateReader(AuctionRooms rooms) {
        this.rooms = rooms;
    }

    public AuctionRoomState read(String code) {
        return rooms.findByCode(code)
            .map(AuctionRoom::readState)
            .orElseThrow(() -> AuctionRoomException.roomNotFound(code));
    }
}
