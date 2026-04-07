package com.naminhyeok.fantazzk.room;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceBid {
    private final Rooms rooms;

    @Transactional
    public RoomBid place(String code, String teamLeaderId, int amount) {
        Room room = rooms.findByCode(code).orElseThrow(() -> RoomException.notFound(code));
        RoomBid bid = room.placeBid(teamLeaderId, amount);
        rooms.save(room);
        return bid;
    }
}
