package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public record RoomPlayerView(
    String name,
    String position,
    int displayOrder,
    String status
) {
    static RoomPlayerView from(RoomPlayer player) {
        return new RoomPlayerView(
            player.getName(),
            player.getPosition(),
            player.getDisplayOrder(),
            player.getStatus().name()
        );
    }

    static RoomPlayerView from(RoomPlayer player, int displayOrder, boolean assigned) {
        return new RoomPlayerView(
            player.getName(),
            player.getPosition(),
            displayOrder,
            assigned ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
        );
    }
}
