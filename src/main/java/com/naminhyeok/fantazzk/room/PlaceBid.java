package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PlaceBid {
    private final Rooms rooms;

    @Transactional
    public RoomBid place(String code, String teamLeaderId, int amount) {
        Room room = rooms.findByCode(code).orElseThrow(() -> CoreException.of(RoomErrorType.ROOM_NOT_FOUND));
        RoomBid bid = room.placeBid(new TeamLeaderId(teamLeaderId), amount);
        rooms.save(room);
        return bid;
    }
}
