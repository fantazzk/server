package com.naminhyeok.fantazzk.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettleAuction {
    private final Rooms rooms;

    @Transactional
    public AuctionSettlement settle(String code) {
        Room room = rooms.findByCode(code).orElseThrow();
        AuctionSettlement settlement = room.settleAuction();
        rooms.save(room);
        return settlement;
    }
}
