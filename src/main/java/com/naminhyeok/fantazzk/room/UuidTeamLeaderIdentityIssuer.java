package com.naminhyeok.fantazzk.room;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class UuidTeamLeaderIdentityIssuer implements TeamLeaderIdentityIssuer {
    @Override
    public TeamLeaderIdentity issue() {
        return new TeamLeaderIdentity(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
    }
}
