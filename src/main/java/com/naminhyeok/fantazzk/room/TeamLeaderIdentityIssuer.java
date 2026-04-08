package com.naminhyeok.fantazzk.room;

interface TeamLeaderIdentityIssuer {
    TeamLeaderIdentity issue();

    record TeamLeaderIdentity(
        String leaderId,
        String actionToken
    ) {
    }
}
