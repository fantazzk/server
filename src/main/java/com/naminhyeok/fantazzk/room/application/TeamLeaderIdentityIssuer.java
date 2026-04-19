package com.naminhyeok.fantazzk.room.application;

public interface TeamLeaderIdentityIssuer {
    public TeamLeaderIdentity issue();

    public record TeamLeaderIdentity(
        String leaderId,
        String actionToken
    ) {
    }
}
