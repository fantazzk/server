package com.naminhyeok.fantazzk.room;

import java.util.UUID;
import lombok.Getter;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;

@Getter
class RoomTeamMember implements Entity<Room, RoomTeamMember.RoomTeamMemberId> {
    private final RoomTeamMemberId id;
    private final String teamLeaderId;
    private final String playerName;
    private final int assignOrder;

    RoomTeamMember(String teamLeaderId, String playerName, int assignOrder) {
        this.id = new RoomTeamMemberId(UUID.randomUUID());
        this.teamLeaderId = teamLeaderId;
        this.playerName = playerName;
        this.assignOrder = assignOrder;
    }

    @Override
    public RoomTeamMemberId getId() {
        return id;
    }

    record RoomTeamMemberId(UUID roomTeamMemberId) implements Identifier {
    }
}
