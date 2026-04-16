package com.naminhyeok.fantazzk.auction;

import java.time.Instant;
import org.jmolecules.ddd.annotation.Service;

@Service
public class AuctionRoomLifecycle {
    private final AuctionRooms rooms;

    public AuctionRoomLifecycle(AuctionRooms rooms) {
        this.rooms = rooms;
    }

    public AuctionRoomState create(String code, String hostLeaderId, String hostNickname, Instant createdAt, AuctionRoomSetup setup) {
        AuctionRoom room = AuctionRoom.create(code, hostLeaderId, hostNickname, createdAt, setup);
        rooms.save(room);
        return room.readState();
    }

    public AuctionRoomState addLeader(String code, String leaderId, String nickname) {
        AuctionRoom room = loadRoom(code);
        room.addLeader(leaderId, nickname);
        rooms.saveAndFlush(room);
        return room.readState();
    }

    public AuctionRoomState start(String code, String hostLeaderId, Instant now) {
        AuctionRoom room = loadRoom(code);
        room.start(hostLeaderId, now);
        rooms.saveAndFlush(room);
        return room.readState();
    }

    public AuctionSettlement settle(String code, Instant now) {
        AuctionRoom room = loadRoom(code);
        AuctionSettlement settlement = room.settle(now);
        rooms.saveAndFlush(room);
        return settlement;
    }

    private AuctionRoom loadRoom(String code) {
        return rooms.findByCode(code).orElseThrow(() -> AuctionRoomException.roomNotFound(code));
    }
}
