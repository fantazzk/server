package com.naminhyeok.fantazzk.room.infrastructure.identity;

import com.naminhyeok.fantazzk.room.application.TeamLeaderIdentityIssuer;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidTeamLeaderIdentityIssuer implements TeamLeaderIdentityIssuer {
    @Override
    public TeamLeaderIdentity issue() {
        return new TeamLeaderIdentity(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString()
        );
    }
}
