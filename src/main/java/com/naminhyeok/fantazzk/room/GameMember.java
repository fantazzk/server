package com.naminhyeok.fantazzk.room;

import java.util.Objects;

record GameMember(
    TeamLeaderId teamLeaderId,
    String playerName,
    int assignOrder
) {
    GameMember {
        Objects.requireNonNull(teamLeaderId, "teamLeaderId must not be null");
        Objects.requireNonNull(playerName, "playerName must not be null");
    }
}
