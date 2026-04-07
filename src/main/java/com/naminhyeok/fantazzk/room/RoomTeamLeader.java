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
    private Integer remainingBudget;

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

    void spend(int amount) {
        if (remainingBudget == null) {
            throw new IllegalStateException("예산이 없는 팀장은 입찰할 수 없습니다");
        }
        if (remainingBudget < amount) {
            throw new IllegalArgumentException("예산이 부족합니다");
        }
        remainingBudget -= amount;
    }

    record RoomTeamLeaderId(UUID roomTeamLeaderId) implements Identifier {
    }
}
