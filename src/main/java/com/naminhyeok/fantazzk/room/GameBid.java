package com.naminhyeok.fantazzk.room;

import java.util.Objects;

record GameBid(
    int round,
    BidSequence sequence,
    TeamLeaderId teamLeaderId,
    int amount
) {
    GameBid {
        Objects.requireNonNull(sequence, "sequence must not be null");
        Objects.requireNonNull(teamLeaderId, "teamLeaderId must not be null");
    }
}
