package com.naminhyeok.fantazzk.room.domain.game;

import com.naminhyeok.fantazzk.room.domain.event.*;
import com.naminhyeok.fantazzk.room.domain.handoff.*;
import com.naminhyeok.fantazzk.room.domain.room.*;
import com.naminhyeok.fantazzk.room.domain.shared.*;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import org.jmolecules.ddd.types.ValueObject;

@Access(AccessType.FIELD)
@Embeddable
@EqualsAndHashCode
public final class RosterMember implements ValueObject {
    @Column(name = "team_leader_id")
    @Convert(converter = TeamLeaderId.JpaConverter.class)
    private TeamLeaderId teamLeaderId;
    @Column(name = "player_name")
    private String playerName;
    @Column(name = "assign_order")
    private int assignOrder;

    RosterMember() {
    }

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
