package com.naminhyeok.fantazzk.room.application.port;

public interface TeamLeaderIdentityIssuer {
    TeamLeaderIdentity issue();

    record TeamLeaderIdentity(
        String leaderId,
        String actionToken
    ) {
    }
}
