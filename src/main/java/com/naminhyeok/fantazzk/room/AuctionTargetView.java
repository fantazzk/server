package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public record AuctionTargetView(
    String name,
    String position
) {
    static AuctionTargetView from(RoomPlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetView(player.getName(), player.getPosition());
    }

    static AuctionTargetView from(GamePlayer player) {
        if (player == null) {
            return null;
        }
        return new AuctionTargetView(player.name(), player.position());
    }
}
