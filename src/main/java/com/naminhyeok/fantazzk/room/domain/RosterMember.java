package com.naminhyeok.fantazzk.room.domain;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jmolecules.ddd.types.ValueObject;

@Access(AccessType.FIELD)
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class RosterMember implements ValueObject {
    @Column(name = "team_leader_id")
    @Convert(converter = TeamLeaderId.JpaConverter.class)
    private TeamLeaderId teamLeaderId;
    @Column(name = "player_name")
    private String playerName;
    @Column(name = "assign_order")
    private int assignOrder;

    public RosterMember(TeamLeaderId teamLeaderId, String playerName, int assignOrder) {
        this.teamLeaderId = teamLeaderId;
        this.playerName = playerName;
        this.assignOrder = assignOrder;
    }

    public TeamLeaderId teamLeaderId() {
        return teamLeaderId;
    }

    public String playerName() {
        return playerName;
    }

    public int assignOrder() {
        return assignOrder;
    }
}
