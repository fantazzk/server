package com.naminhyeok.fantazzk.room;

import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

@Getter
class RoomTeamLeader implements Entity<Room, RoomTeamLeader.RoomTeamLeaderId> {
    private final RoomTeamLeaderId id;
    private final String teamLeaderId;
    private final String nickname;
    private final Integer remainingBudget;

    RoomTeamLeader(String teamLeaderId, String nickname, Integer remainingBudget) {
        this.id = new RoomTeamLeaderId(UUID.randomUUID());
        this.teamLeaderId = teamLeaderId;
        this.nickname = nickname;
        this.remainingBudget = remainingBudget;
    }

    @Override
    public RoomTeamLeaderId getId() {
        return id;
    }

    record RoomTeamLeaderId(UUID roomTeamLeaderId) implements Identifier {
    }
}
