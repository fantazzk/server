package com.naminhyeok.fantazzk.room;

import com.naminhyeok.fantazzk.room.domain.game.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.repository.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

public record GamePlayerView(
    String name,
    String position,
    int displayOrder,
    String status
) {
    static GamePlayerView from(GamePlayer player, int displayOrder, boolean assigned) {
        return new GamePlayerView(
            player.name(),
            player.position(),
            displayOrder,
            assigned ? PlayerStatus.ASSIGNED.name() : PlayerStatus.AVAILABLE.name()
        );
    }
}
